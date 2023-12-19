package ru.shop.backend.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.shop.backend.search.api.SearchApi;
import ru.shop.backend.search.dto.SearchResultDto;
import ru.shop.backend.search.dto.SearchResultElasticDto;
import ru.shop.backend.search.service.SearchService;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {

    private final SearchService service;
    public ResponseEntity<SearchResultDto> findResultByTextAndRegion(String text, int regionId) {
        return ResponseEntity.ok().body(service.getElasticSearchResult(regionId,  text));
    }

    public ResponseEntity<SearchResultElasticDto> findResultByText(String text) {
        return ResponseEntity.ok().body(service.getElasticSearchResult(text));
    }


}
