package ru.shop.backend.search.search.strategy.impl;

import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.search.strategy.SearchStrategy;

import java.util.List;

import static ru.shop.backend.search.helper.TextConverter.convert;

public class CatalogueTypeStrongSearchStrategy extends SearchStrategy {
    public CatalogueTypeStrongSearchStrategy(ItemRepository repo) {
        super(repo);
    }

    @Override
    public List<ItemElastic> search(String text, String brand, String type, Long catalogueId, Pageable pageable) {
        List<ItemElastic> list = repo.findAllByCatalogueAndType(text, catalogueId, pageable);
        if (list.isEmpty()) {
            list = repo.findAllByCatalogueAndType(convert(text), catalogueId, pageable);
        }
        return list;
    }

}
