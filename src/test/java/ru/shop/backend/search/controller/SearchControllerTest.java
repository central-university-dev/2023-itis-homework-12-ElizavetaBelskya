package ru.shop.backend.search.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ElasticsearchTestContainer;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
//@Testcontainers
public class SearchControllerTest {

    //очень надоело запускать тестконтейнеры, поэтому использовала запущенные через compose

//    @BeforeAll
//    static void beforeAll() {
//        database.start();
//        elasticsearchContainer.start();
//    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemJpaRepository itemJpaRepository;

//    @Container
//    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchTestContainer().withReuse(true);;
//
//    @Container
//    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:12.9-alpine")
//            .withPassword("12345").withUsername("postgres").withReuse(true);;
//
//
//    @DynamicPropertySource
//    static void postgresqlProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", database::getJdbcUrl);
//        registry.add("spring.datasource.password", database::getPassword);
//        registry.add("spring.datasource.username", database::getUsername);
//        registry.add( "spring.elasticsearch.rest.uris", elasticsearchContainer::getHttpHostAddress);
//        registry.add("spring.elasticsearch.rest.username", () -> "elastic");
//        registry.add( "spring.elasticsearch.rest.password", () -> ElasticsearchTestContainer.ELASTIC_PASSWORD);
//    }


    @AfterEach
    public void clearAll() throws IOException {
        itemRepository.deleteAll();
        ClassPathResource resource = new ClassPathResource("clear.sql");
        String scriptContent = Files.readString(resource.getFile().toPath());
        jdbcTemplate.execute(scriptContent);
    }

    @Test
    public void test_get_with_null_text_should_return_bad_request() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"classpath:test_1.sql"})
    public void test_item_with_sku_in_jpa_then_return_ok_and_CatalogueElasticDto() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(111L).name("name")
                .catalogue("catalogue")
                .catalogueId(1L)
                .brand("brand").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "111"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].name").value("Catalogue 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].catalogueId").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].name").value("Product 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].itemId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].brand").value("Brand 1"));
    }

    @Test
    public void test_item_with_numeric_text_contains_name_then_return_ok_and_CatalogueElasticDto() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(1L).name("aoaooa555fdwq")
                .catalogue("catalogue")
                .catalogueId(1L)
                .brand("brand").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "555"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].name").value("catalogue"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].catalogueId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].name").value("aoaooa555fdwq"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].itemId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].brand").value("brand"));;
    }

    @Test
    public void test_item_with_not_numeric_text_contains_name_and_type_then_return_ok_and_CatalogueElasticDto() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(1L).name("name")
                .catalogue("catalogue").type("type")
                .catalogueId(1L).fulltext("name type")
                .brand("brand").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "name type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].name").value("catalogue"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].catalogueId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].itemId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].brand").value("brand"));
    }

    @Test
    public void test_item_with_not_numeric_text_contains_name_then_return_ok_and_CatalogueElasticDto() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(1L).name("name")
                .catalogue("catalogue 1").type("type")
                .catalogueId(1L).fulltext("name type")
                .brand("brand").build());
        itemRepository.save(ItemElastic.builder()
                .itemId(2L).name("wrong")
                .catalogue("catalogue 2").type("wrong")
                .catalogueId(1L).fulltext("wrong wrong wrong ")
                .brand("brand 2").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "name"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].name").value("catalogue 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].catalogueId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].itemId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].items[0].brand").value("brand"));
    }

    @Test
    public void test_item_with_not_numeric_text_contains_catalogue_then_return_ok_and_CatalogueElasticDto_with_empty_list() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(1L).name("name")
                .catalogue("catalogue 1").type("type")
                .catalogueId(1L).fulltext("name type")
                .brand("brand").build());
        itemRepository.save(ItemElastic.builder()
                .itemId(2L).name("wrong")
                .catalogue("test").type("wrong")
                .catalogueId(1L).fulltext("wrong wrong wrong ")
                .brand("brand 2").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").isEmpty());
    }

    @Test
    public void test_text_contains_errors() throws Exception {
        itemRepository.save(ItemElastic.builder()
                .itemId(1L).name("name")
                .catalogue("catalogue 1").type("type")
                .catalogueId(1L).fulltext("name type")
                .brand("brand").build());
        itemRepository.save(ItemElastic.builder()
                .itemId(2L).name("wrong")
                .catalogue("test").type("wrong")
                .catalogueId(1L).fulltext("wrong wrong wrong ")
                .brand("brand 2").build());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/search/by").param("text", "test["))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").isEmpty());
    }


//    @AfterAll
//    public static void stop() {
//        elasticsearchContainer.stop();
//        database.stop();
//    }


}
