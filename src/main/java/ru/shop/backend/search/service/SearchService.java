package ru.shop.backend.search.service;

import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.dto.*;
import ru.shop.backend.search.helper.TextConverter;
import ru.shop.backend.search.model.Item;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.search.strategy.ComplexSearchHandler;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class SearchService {

    @Builder
    @Getter
    private static class GetAllResult {
        private String type;
        private List<ItemElastic> list;
        private String brand;
        private Long catalogueId;
        private String text;
        private String catalogue;
    }

    private final ItemRepository itemRepo;

    private final ItemJpaRepository itemJpaRepo;
    private final Pageable pageable = PageRequest.of(0, 150);
    private final Pageable pageableSmall = PageRequest.of(0, 10);

    private final ComplexSearchHandler searchHandler;

    //метод, используемый в контроллере
    public SearchResultElasticDto getElasticSearchResult(String text) {
        return new SearchResultElasticDto(getCataloguesBySkuOrName(text, pageable));
    }


    //метод, используемый в контроллере
    public SearchResultDto getElasticSearchResult(Integer regionId, String text) {
        List<CatalogueElasticDto> result = getCataloguesBySkuOrName(text, pageableSmall);
        //ищем хоть какой-то результат

        //по результатам строим items
        List<Item> items = itemJpaRepo.findByIds(regionId, result.stream()
                        .flatMap(category -> category.getItems().stream())
                        .map(ItemElastic::getItemId)
                        .collect(Collectors.toList()))
                .stream()
                .map(Item::new)
                .collect(Collectors.toList());

        //достаем бренд из результата (результат тоже может быть пуст)
        String finalBrand = result.isEmpty() ? "" : result.get(0).getBrand().toLowerCase(Locale.ROOT);

        //опять достаем items
        List<Integer> itemIds = items.stream()
                .map(Item::getItemId)
                .collect(Collectors.toList());

        //ищем категории по itemIds
        List<Object[]> catResults = itemJpaRepo.findCatsByIds(itemIds);

        // Строим уникальные URL для категорий
        Set<String> uniqueCatUrls = new HashSet<>();
        List<CategoryDto> categories = catResults.stream()
                .map(arr -> {
                    String catUrl = arr[2].toString();
                    if (uniqueCatUrls.add(catUrl)) {
                        String categoryUrl = "/cat/" + catUrl + (finalBrand.isEmpty() ? "" : "/brands/" + finalBrand);
                        return new CategoryDto(arr, categoryUrl);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return SearchResultDto.createSearchResultDto(items, categories, result);
    }

    private List<CatalogueElasticDto> getCataloguesBySkuOrName(String text, Pageable pageable) {
        if (TextConverter.isNumeric(text)) {
            //ищем элемент подходящий по sku и достаем его itemId
            Optional<Integer> itemId = itemJpaRepo.findBySku(text).stream().findFirst();
            //если нашли то достаем все элементы по id и создаем каталоги
            if (itemId.isPresent()) {
                List<ItemElastic> list = itemRepo.findAllByItemId(String.valueOf(itemId.get()), PageRequest.of(0, 1));
                return createCatalogueElasticFromLIst(list);
            } else {
                //если не нашли то ищем каталог по имени
                List<CatalogueElasticDto> catalogues = getCataloguesByItemName(text);
                if (!catalogues.isEmpty()) {
                    return catalogues;
                }
            }
        }

        //если не нашли ничего то ищем по всем показателям
        return getCataloguesByTextByAllParameters(text, pageable);
    }

    public List<CatalogueElasticDto> getCataloguesByTextByAllParameters(String text, Pageable pageable) {
        String type = "";
        String brand = "";
        String textCopy = text.concat("");
        Long catalogueId;
        boolean needConvert;

        Pair<Boolean, String> res = convertIfNeeded(text);
        needConvert = res.getLeft();
        text = res.getRight();

        if (text.contains(" ")) {
            //вот в этом блоке мы можем получить brand, может поменяться текст, так как мы исключаем тот текст, по которому нашли бренд
            GetAllResult result = searchByQueryWords(text, needConvert, brand);
            text = result.getText();
            brand = result.getBrand();
            //по сути мы тут получаем только бренд !
        }

        GetAllResult result = findAllByType(text, type, pageable, needConvert);
        text = result.getText();
        type = result.getType();
        if (!brand.isEmpty()) {
            catalogueId = result.getCatalogueId();
            String catalogue = result.getCatalogue();

            //после этого метода у нас уже новый лист (возможно пустой), найденный по типу, и если он не пуст, то и тип

            //если после этого у нас текст закончился и бренд не пустой, то все
            //мы нашли
            if (text.trim().isEmpty()) //тут еще null зачем-то
                return Collections.singletonList(new CatalogueElasticDto(catalogue, catalogueId, null, brand));

        } else {
            catalogueId = searchCatalogueId(text, needConvert);
        }

        text = text.trim();
        text += "?"; //добавляем знак вопроса к нашему непустому тексту, чтобы расширить поиск
        List<ItemElastic> list = updateList(text, type, brand, catalogueId, pageable);

        //если в прошлом методе ничего не нашли

        if (list.isEmpty()) {
            if (textCopy.contains(" ")) {
                text = String.join(" ", textCopy.split("\\s"));
            }
            textCopy += "?";
            list  = itemRepo.findAllFulltextNotStrong(textCopy, pageable);
            if (list.isEmpty() && needConvert) {
                list = itemRepo.findAllByTypeAndBrand(TextConverter.convert(textCopy), brand, pageable);
            }
        }

        String cleanedName = text.replace("?", "");

        return getCataloguesByLastItemOrFromMap(list, cleanedName, brand);
    }


    //если изначальный текст содержит ошибки, то он меняется на конвертированный и needConvert = false;
    //если только конвертированный текст содержит ошибки, мы оставляем изначальный текст, needConvert = false
    //если и там и там нет ошибок, то needConvert = true (можно использовать текст для конвертации)
    private Pair<Boolean, String> convertIfNeeded(String text) {
        boolean normalTextHasErrors = TextConverter.isContainErrorChar(text);
        boolean convertedTextHasErrors = TextConverter.isContainErrorChar(TextConverter.convert(text));

        //думаю стоит ввести такое ограничение
        if (normalTextHasErrors && convertedTextHasErrors) {
            throw new IllegalArgumentException("Both normal and converted texts contain errors.");
        }

        if (normalTextHasErrors) {
            return Pair.of(false, TextConverter.convert(text));
        } else if (convertedTextHasErrors) {
            return Pair.of(false, text);
        }

        return Pair.of(true, text);
    }



    //строим лист заново исходя из показателей, и даже неважно значение needConvert
    private List<ItemElastic> updateList(String text, String type, String brand, Long catalogueId, Pageable pageable) {
        return searchHandler.findListByAllParameters(text, type, brand, catalogueId, pageable);
    }

    //теперь ищем catalogueId
    private Long searchCatalogueId(String text, boolean needConvert) {
        List<ItemElastic>  list = itemRepo.findByCatalogue(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = itemRepo.findByCatalogue(TextConverter.convert(text), pageable);
        }
        return list.stream().findFirst().map(ItemElastic::getCatalogueId).orElse(null);
    }


    // если у нас текст с пробелами
    // вызываем на каждом слове findAllFulltextNotStrong, если не находим по слову - ищем по конвертированному
    // как только находим items для листа - исключаем слово из text и берем brand у первого элемента
    private GetAllResult searchByQueryWords(String text, boolean needConvert, String brand) {
        for (String queryWord: text.split("\\s")) {
            brand = findBrandInFirstItem(queryWord);
            if (brand.isEmpty() && needConvert) {
                brand = findBrandInFirstItem(TextConverter.convert(queryWord));
            }

            if (!brand.isEmpty()) {
                text = text.replace(queryWord, "").trim().replace("  ", " ");
                break;
            }
        }
        return GetAllResult.builder().brand(brand).text(text).build();
    }

    private String findBrandInFirstItem(String text) {
        List<ItemElastic> resultList = itemRepo.findAllFulltextNotStrong(text, PageRequest.of(0, 1));
        return resultList.isEmpty() ? "" : resultList.get(0).getBrand();
    }

    //пытаемся найти items по типу, если находим то выбираем самый короткий по длине слова тип для type
    //если пустой то делаем все то же самое, только по отдельным словам
    private GetAllResult findAllByType(String text, String type, Pageable pageable, boolean needConvert) {
        var list = itemRepo.findAllByType(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = itemRepo.findAllByType(TextConverter.convert(text), pageable);
        }

        if (list.isEmpty() && text.contains(" ")) {
            for (String queryWord : text.split("\\s")) {
                list = itemRepo.findAllByType(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = itemRepo.findAllByType(TextConverter.convert(queryWord), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "");
                }
            }
            //по итогу у нас пустой текст в этой ветви, list тоже может быть пустым
        }

        if (list.isEmpty()) {
            return GetAllResult.builder().text(text).type(type).build();
        } else {
            type = (list.stream().map(ItemElastic::getType).min(Comparator.comparingInt(String::length)).get());
            return GetAllResult
                    .builder()
                    .text(text)
                    .catalogueId(list.get(0).getCatalogueId())
                    .catalogue(list.get(0).getCatalogue())
                    .type(type).build();
        }
    }


    private List<CatalogueElasticDto> getCataloguesByLastItemOrFromMap(List<ItemElastic> list, String text, String brand) {
        String finalBrand = brand.isEmpty()? null : brand;

        //ищем последний подходящий элемент
        Optional<ItemElastic> matchingItem = list.stream()
                .filter(item -> text.equals(item.getName()) || (text.endsWith(item.getName()) && text.startsWith(item.getType())))
                .reduce((first, second) -> second);


        if (matchingItem.isPresent()) {
            var item = matchingItem.get();
            return createCatalogueElasticFromLIst(List.of(item), finalBrand);
        }

        //иначе формируем мапу
        Map<Optional<String>, List<ItemElastic>> map = list.stream()
                .collect(groupingBy(itemElastic -> Optional.ofNullable(itemElastic.getCatalogue())));

        return map.entrySet()
                .stream()
                .map(entry -> new CatalogueElasticDto(
                        entry.getKey().orElse(null),
                        entry.getValue().get(0).getCatalogueId(),
                        entry.getValue(),
                        finalBrand
                ))
                .collect(Collectors.toList());
    }


    private List<CatalogueElasticDto> getCataloguesByItemName(String name) {
        List<ItemElastic> list = itemRepo.findAllByName(".*" + name + ".*", pageable);
        return getCataloguesByLastItemOrFromMap(list, name, "");
    }

    //создается список каталогов по списку или возвращается пустой список
    private List<CatalogueElasticDto> createCatalogueElasticFromLIst(List<ItemElastic> list) {
        if (!list.isEmpty()) {
            ItemElastic firstItem = list.get(0);
            CatalogueElasticDto catalogueElasticDto = new CatalogueElasticDto(
                    firstItem.getCatalogue(),
                    firstItem.getCatalogueId(),
                    list,
                    firstItem.getBrand()
            );
            return Collections.singletonList(catalogueElasticDto);
        } else {
            return Collections.emptyList();
        }
    }

    private List<CatalogueElasticDto> createCatalogueElasticFromLIst(List<ItemElastic> list, String brand) {
        ItemElastic firstItem = list.get(0);
        CatalogueElasticDto catalogueElasticDto = new CatalogueElasticDto(
                firstItem.getCatalogue(),
                firstItem.getCatalogueId(),
                list,
                brand
        );
        return Collections.singletonList(catalogueElasticDto);
    }

}
