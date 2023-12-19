package ru.shop.backend.search.search.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.search.strategy.*;
import ru.shop.backend.search.search.strategy.impl.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ComplexSearchHandler {

    private final ItemRepository repo;

    private SearchStrategy chooseStrategy(String brand, Long catalogueId, String type) {
        SearchStrategy strategy;
        if (brand.isEmpty()) {
            if (catalogueId == null) {
                strategy = type.isEmpty() ? new CatalogueTypeDescriptionSearchStrategy(repo) : new TypeDescriptionSearchStrategy(repo);
            } else {
                strategy = type.isEmpty() ? new CatalogueTypeStrongSearchStrategy(repo) : new CatalogueTypeSearchStrategy(repo);
            }
        } else {
            strategy = type.isEmpty() ? new BrandNameDescriptionTypeSearchStrategy(repo) : new TypeBrandSearchStrategy(repo);
        }
        return strategy;
    }

    public List<ItemElastic> findListByAllParameters(String text, String type, String brand, Long catalogueId, Pageable pageable) {
        SearchStrategy strategy = chooseStrategy(brand, catalogueId, type);
        return strategy.search(text, brand, type, catalogueId, pageable);
    }



}
