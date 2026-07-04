package com.kyro.catalog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FilterProduct {
    private String topLevelCategory;
    private String secondLevelCategory;
    private String color;
    private Integer minPrice;
    private Integer maxPrice;
    private String sort;
    private String keyword;
    private String brand;
}
