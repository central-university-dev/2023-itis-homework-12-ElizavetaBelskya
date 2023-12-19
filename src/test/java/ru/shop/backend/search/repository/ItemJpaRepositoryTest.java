package ru.shop.backend.search.repository;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.shop.backend.search.model.ItemEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = ItemJpaRepositoryTest.DataSourceInitializer.class)
public class ItemJpaRepositoryTest {

    @Autowired
    private ItemJpaRepository itemJpaRepository;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:12.9-alpine");

    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + database.getJdbcUrl(),
                    "spring.datasource.username=" + database.getUsername(),
                    "spring.datasource.password=" + database.getPassword(),
                    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect",
                    "spring.datasource.driver-class-name=org.postgresql.Driver"
            );

        }
    }

    @Test
    public void test_findByIds_when_items_exist_then_return_correct_items() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        List<Object[]> items = itemJpaRepository.findByIds(1, ids);
        assertNotNull(items);
        assertEquals(3, items.size());
        for (Object[] item : items) {
            assertNotNull(item);
            assertEquals(6, item.length);
            assertTrue(item[0] instanceof Integer);
            assertTrue(item[1] instanceof String);
            assertTrue(item[2] instanceof BigDecimal);
            assertTrue(item[3] instanceof String);
            assertTrue(item[4] instanceof String);
            assertTrue(item[5] instanceof String);
        }

        Object[] firstResult = items.get(0);
        assertEquals(1, firstResult[0]);
        assertEquals("Product 1", firstResult[1]);
        assertEquals(new BigDecimal("10.99"), firstResult[2]);
    }


    @Test
    public void test_findByIds_when_no_items_then_return_empty_list() {
        List<Long> ids = Collections.emptyList();
        List<Object[]> items = itemJpaRepository.findByIds(1, ids);
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    public void test_findByIds_when_incorrect_items_then_return_empty_list() {
        List<Long> ids = Arrays.asList(22L, 36L);
        List<Object[]> items = itemJpaRepository.findByIds(1, ids);
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    public void test_findBySku_then_return_correct_item_id() {
        String skuToSearch = "SKU001";
        List<Integer> result = itemJpaRepository.findBySku(skuToSearch);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    public void test_findBySku_when_sku_is_not_found_then_return_empty_list() {
        String skuToSearch = "SKU0";
        List<Integer> result = itemJpaRepository.findBySku(skuToSearch);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findCatsByIds_when_data_is_correct_then_return_correct_items() {
        List<Integer> itemIds = Arrays.asList(1, 2, 3);
        List<Object[]> result = itemJpaRepository.findCatsByIds(itemIds);

        assertNotNull(result);
        assertEquals(3, result.size());

        for (Object[] category : result) {
            assertNotNull(category);
            assertEquals(5, category.length);
            assertTrue(category[0] instanceof String);
            assertTrue(category[1] instanceof String);
            assertTrue(category[2] instanceof String);
            assertTrue(category[3] instanceof String);
            assertTrue(category[4] instanceof String);
        }

        Object[] firstResult = result.get(0);
        assertEquals("Subcategory 1", firstResult[0]);
        assertEquals("Category 1", firstResult[1]);
        assertEquals("subcategory1", firstResult[2]);
    }

    @Test
    public void test_findCatsByIds_when_item_ids_is_not_found_then_return_correct_items() {
        List<Integer> itemIds = Arrays.asList(12, 93);
        List<Object[]> result = itemJpaRepository.findCatsByIds(itemIds);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findAllInStream() {
        Stream<ItemEntity> resultStream = itemJpaRepository.findAllInStream();
        List<ItemEntity> resultItems = resultStream.collect(Collectors.toList());
        assertNotNull(resultItems);
        assertEquals(3, resultItems.size());

        ItemEntity resultItem1 = resultItems.get(0);
        assertEquals("Product 1", resultItem1.getName());
        assertEquals("Brand 1", resultItem1.getBrand());
        assertEquals("Catalogue 1", resultItem1.getCatalogue());
        assertEquals("Type 1", resultItem1.getType());
        assertEquals("Description 1", resultItem1.getDescription());
        assertEquals(1, resultItem1.getBrandId());
        assertEquals(3, resultItem1.getCatalogueId());
        assertEquals(1, resultItem1.getItemId());

        ItemEntity resultItem2 = resultItems.get(1);
        assertEquals("Product 2", resultItem2.getName());
        assertEquals("Brand 2", resultItem2.getBrand());
        assertEquals("Catalogue 2", resultItem2.getCatalogue());
        assertEquals("Type 2", resultItem2.getType());
        assertEquals("Description 2", resultItem2.getDescription());
        assertEquals(2, resultItem2.getBrandId());
        assertEquals(4, resultItem2.getCatalogueId());
        assertEquals(2, resultItem2.getItemId());
    }


}
