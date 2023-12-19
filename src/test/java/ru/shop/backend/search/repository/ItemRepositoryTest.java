package ru.shop.backend.search.repository;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.shop.backend.search.model.ItemElastic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@DataElasticsearchTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = ItemRepositoryTest.DataSourceInitializer.class)
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Container
    private static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchTestContainer();

    @BeforeAll
    static void setUp() {
        elasticsearchContainer.start();
    }

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {

                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                        applicationContext,
                        "spring.elasticsearch.rest.uris=" + elasticsearchContainer.getHttpHostAddress(),
                        "spring.elasticsearch.rest.username=elastic",
                        "spring.elasticsearch.rest.password=123456"
                );

        }
    }

    @Test
    public void test_find_then_return_correct_result() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Бренд1")
                .type("Тип1")
                .name("Имя1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Имя2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);
        String searchTerm = "Тип1";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findByCatalogueAndTypeAndDescriptionStrong(searchTerm, pageable);

        assertEquals(2, result.size());
        assertEquals("Имя1", result.get(0).getName());
        assertEquals("Имя2", result.get(1).getName());
    }

    @Test
    public void test_find_when_name_is_not_found_then_return_empty_list() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Бренд1")
                .type("Тип1")
                .name("Имя1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Имя2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);
        String searchTerm = "blalala";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findByCatalogueAndTypeAndDescriptionStrong(searchTerm, pageable);

        assertEquals(0, result.size());
    }


    @Test
    public void test_findAllByType_then_return_correct_result() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Бренд1")
                .type("Точно 1")
                .name("Имя1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Имя2")
                .description("Описание2")
                .build();
        repository.save(item1);
        repository.save(item2);
        String searchTerm = "Точно 1";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findAllByType(searchTerm, pageable);
        assertEquals(1, result.size());
        assertEquals("Имя1", result.get(0).getName());
    }

    @Test
    public void test_findAllByType_when_name_is_not_found_then_return_empty_list() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Бренд1")
                .type("Тип1")
                .name("Имя1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Имя2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);
        String searchTerm = "ammaladnjs";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findAllByType(searchTerm, pageable);
        assertEquals(0, result.size());
    }

    @Test
    public void test_findAllByBrand_then_return_correct_result() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Топ 1")
                .type("Тип1")
                .name("Наименование1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Наименование2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);

        String term = "Топ 1";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findAllByBrand(term, pageable);

        assertEquals(1, result.size());
        assertEquals("Наименование1", result.get(0).getName());
    }


    @Test
    void test_findAllByBrand() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Топ 1")
                .type("Тип1")
                .name("Наименование1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Наименование2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);

        String searchTerm = "Топ 1";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> resultHighFuzzinessAndBoost = repository.findAllByBrand(searchTerm, pageable);

        assertEquals(1, resultHighFuzzinessAndBoost.size());
        assertEquals("Наименование1", resultHighFuzzinessAndBoost.get(0).getName());
    }

    @Test
    void test_findAllByName_with_regex() {
        ItemElastic item1 = ItemElastic.builder()
                .itemId(1L)
                .catalogueId(101L)
                .catalogue("Каталог1")
                .brand("Бренд1")
                .type("Тип1")
                .name("Наименование1")
                .description("Описание1")
                .build();
        ItemElastic item2 = ItemElastic.builder()
                .itemId(2L)
                .catalogueId(102L)
                .catalogue("Каталог2")
                .brand("Бренд2")
                .type("Тип2")
                .name("Наименование2")
                .description("Описание2")
                .build();

        repository.save(item1);
        repository.save(item2);

        String searchTermRegex = "Наименование[0-9]";
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemElastic> result = repository.findAllByName(searchTermRegex, pageable);

        assertEquals(2, result.size());
    }



}
