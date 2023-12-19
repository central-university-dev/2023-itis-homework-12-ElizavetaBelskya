package ru.shop.backend.search.search.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.List;

@RequiredArgsConstructor
public abstract class SearchStrategy {

    protected final ItemRepository repo;
    public abstract List<ItemElastic> search(String text, String brand, String type, Long catalogueId, Pageable pageable);

}