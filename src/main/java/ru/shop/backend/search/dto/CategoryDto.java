package ru.shop.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {

    private String name;
    private String parentName;
    private String url;
    private String parentUrl;
    private String image;

    public CategoryDto(Object[] arr, String categoryUrl) {
        this(arr[0].toString(), arr[1].toString(), categoryUrl, "/cat/" + arr[3].toString(), arr[4] == null ? null : arr[4].toString());
    }


}
