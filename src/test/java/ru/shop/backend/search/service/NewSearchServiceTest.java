package ru.shop.backend.search.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.dto.CatalogueElasticDto;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.search.strategy.ComplexSearchHandler;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.shop.backend.search.service.OldSearchService.convert;

@ExtendWith(MockitoExtension.class)
public class NewSearchServiceTest {

    private final Pageable pageable = PageRequest.of(0, 150);

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemJpaRepository itemJpaRepository;

    @InjectMocks
    private SearchService searchService;

    @BeforeEach
    public void setUp() {
        searchService = new SearchService(itemRepository, itemJpaRepository, new ComplexSearchHandler(itemRepository));
    }

    @Test
    void givenTextWithBrand_whenGetCataloguesByText_thenReturnsCatalogueWithNullBrand() {
        ItemElastic mockItem = new ItemElastic();
        mockItem.setBrand("MockBrand");
        mockItem.setType("Type");
        mockItem.setCatalogue("catalog");
        mockItem.setCatalogueId(2L);
        mockItem.setName("Name");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        when(itemRepository.findAllFulltextNotStrong(any(), any())).thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters("MockText", pageable);

        assertEquals(1, result.size());
        assertEquals(null, result.get(0).getBrand());
    }

    @Test
    void givenTextWithType_whenGetCataloguesByText_thenReturnsEmptyList() {
        ItemElastic mockItem = new ItemElastic();
        mockItem.setType("MockType");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        when(itemRepository.findAllByType(any(), any())).thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters("MockText", pageable);

        assertEquals(0, result.size()); //мы сам список не достали, поэтому результат имеет длину 0
    }

    @Test
    void givenTextAndCatalogueId_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "MockText";
        ItemElastic mockItem = new ItemElastic();
        mockItem.setCatalogueId(1L);
        mockItem.setName("item");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        when(itemRepository.findByCatalogue(any(), any())).thenReturn(mockItemList);

        when(itemRepository.findAllByCatalogueAndType(eq(text + "?"), eq(mockItem.getCatalogueId()), eq(pageable)))
                .thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1L, result.get(0).getCatalogueId());
    }

    @Test
    void givenText_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "MockText";
        ItemElastic mockItem = new ItemElastic();
        mockItem.setCatalogueId(1L);
        mockItem.setName("item");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        when(itemRepository.findByCatalogueAndTypeAndDescriptionStrong(eq(text + "?"), eq(pageable))).thenReturn(mockItemList);
        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1L, result.get(0).getCatalogueId());
    }

    @Test
    void givenTextAndBrand_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "MockText";
        ItemElastic mockItem1 = new ItemElastic();
        mockItem1.setCatalogueId(1L);
        mockItem1.setName("kText");
        mockItem1.setType("Mo");
        ItemElastic mockItem2 = new ItemElastic();
        mockItem2.setCatalogueId(2L);
        mockItem2.setName("Text");
        mockItem2.setType("Mock");
        List<ItemElastic> mockItemList = List.of(mockItem1, mockItem2);
        when(itemRepository.findByCatalogueAndTypeAndDescriptionStrong(eq(text + "?"), eq(pageable))).thenReturn(mockItemList);
        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2L, result.get(0).getCatalogueId());
    }

    @Test
    void givenTextAndBrandAndType_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "Mock text";
        ItemElastic mockItem = new ItemElastic();
        mockItem.setCatalogueId(1L);
        mockItem.setName("item");
        mockItem.setBrand("brand");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        Pageable pageableSmall = PageRequest.of(0, 1);

        when(itemRepository.findAllFulltextNotStrong(eq("text"), eq(pageableSmall))).thenReturn(mockItemList);
        when(itemRepository.findAllFulltextNotStrong(eq("Mock"), eq(pageableSmall))).thenReturn(Collections.emptyList());
        when(itemRepository.findAllFulltextNotStrong(eq(convert("Mock")), eq(pageableSmall))).thenReturn(Collections.emptyList());
        when(itemRepository.findByBrandAndNameDescriptionType(eq("Mock?"), eq("brand"), eq(pageable))).thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(1L, result.get(0).getCatalogueId());
    }

    @Test
    void givenTextAndTypeAndBrand_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "Mock text";
        ItemElastic mockItem = new ItemElastic();
        mockItem.setName("item");
        mockItem.setBrand("brand");
        mockItem.setCatalogueId(3L);
        mockItem.setType("My type");
        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);
        Pageable pageableSmall = PageRequest.of(0, 1);

        when(itemRepository.findAllFulltextNotStrong(eq("text"), eq(pageableSmall))).thenReturn(mockItemList);
        when(itemRepository.findAllFulltextNotStrong(eq("Mock"), eq(pageableSmall))).thenReturn(Collections.emptyList());
        when(itemRepository.findAllFulltextNotStrong(eq(convert("Mock")), eq(pageableSmall))).thenReturn(Collections.emptyList());
        when(itemRepository.findAllByType(eq("Mock"), eq(pageable))).thenReturn(mockItemList);
        when(itemRepository.findAllByTypeAndBrand(eq("Mock?"), eq("brand"), eq(pageable))).thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(3L, result.get(0).getCatalogueId());
    }

    @Test
    void givenTextAndCatalogue_whenGetCataloguesByText_thenReturnsCatalogueWithMatchingId() {
        String text = "Mock text";
        ItemElastic mockItem = new ItemElastic();
        mockItem.setName("item");
        mockItem.setBrand("brand");
        mockItem.setCatalogueId(3L);
        mockItem.setCatalogue("catalogue 3");

        List<ItemElastic> mockItemList = Collections.singletonList(mockItem);

        when(itemRepository.findByCatalogue(eq("Mock text"), eq(pageable))).thenReturn(mockItemList);
        when(itemRepository.findAllByCatalogueAndType(eq("Mock text?"), eq(3L), eq(pageable))).thenReturn(mockItemList);

        List<CatalogueElasticDto> result = searchService.getCataloguesByTextByAllParameters(text, pageable);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(3L, result.get(0).getCatalogueId());
    }

}
