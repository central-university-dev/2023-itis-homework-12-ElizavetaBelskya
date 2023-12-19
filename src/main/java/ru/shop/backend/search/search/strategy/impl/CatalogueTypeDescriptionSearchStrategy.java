package ru.shop.backend.search.search.strategy.impl;

import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.search.strategy.SearchStrategy;

import java.util.List;

import static ru.shop.backend.search.helper.TextConverter.convert;


public class CatalogueTypeDescriptionSearchStrategy extends SearchStrategy {

    public CatalogueTypeDescriptionSearchStrategy(ItemRepository repo) {
        super(repo);
    }

    @Override
    public List<ItemElastic> search(String text, String brand, String type, Long catalogueId, Pageable pageable) {
        //вообще у этого метода российский анализатор, поэтому тут лучше выполнять только для русских слов, иначе результат непредсказуем
        List<ItemElastic> list = repo.findByCatalogueAndTypeAndDescriptionStrong(text, pageable);
        if (list.isEmpty()) {
            list = repo.findByCatalogueAndTypeAndDescriptionStrong(convert(text), pageable);
        }
        return list;
    }

}

