package ru.shop.backend.search.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.shop.backend.search.dto.SearchResultDto;
import ru.shop.backend.search.dto.SearchResultElasticDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequestMapping("/api/search")
@Tag(name = "Поиск", description = "Методы поиска")
public interface SearchApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает результаты поиска для всплывающего окна",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchResultDto.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка обработки",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content)})
    @Parameter(name = "text", description = "Поисковый запрос")
    @GetMapping
    ResponseEntity<SearchResultDto> findResultByTextAndRegion(@RequestParam @NotBlank String text,
                                                              @CookieValue(name="regionId", defaultValue="1") int regionId);


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает результаты поиска",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchResultElasticDto.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка обработки",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content)})
    @Parameter(name = "text", description = "Поисковый запрос")
    @RequestMapping(method = GET, value = "/by", produces = "application/json;charset=UTF-8")
    ResponseEntity<SearchResultElasticDto> findResultByText(@RequestParam @NotBlank String text);

}
