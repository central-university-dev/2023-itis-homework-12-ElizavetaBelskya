package ru.shop.backend.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.shop.backend.search.model.Item;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SearchResultDto {

    private List<Item> items;
    private List<CategoryDto> categories;
    private List<TypeHelpText> typeQueries;

    public static SearchResultDto createSearchResultDto(List<Item> items, List<CategoryDto> categories, List<CatalogueElasticDto> catalogueElasticDtos) {
        return new SearchResultDto(items, categories, buildTypeHelpTextList(catalogueElasticDtos));
    }

    public SearchResultDto(List<Item> items, List<CategoryDto> categories, List<TypeHelpText> typeQueries) {
        this.items = items;
        this.categories = categories;
        this.typeQueries = typeQueries;
    }



    private static List<TypeHelpText> buildTypeHelpTextList(List<CatalogueElasticDto> result) {
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            String itemType = result.get(0).getItems().get(0).getType();
            String brand = result.get(0).getBrand();
            String text = ((itemType != null? itemType : "") + " " + (brand != null? brand : "")).trim();
            return (List.of(new TypeHelpText(TypeOfQuery.SEE_ALSO, text)));
        }
    }

}
