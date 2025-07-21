package com.example.productsreview.domain.enumeration;

import lombok.Getter;

@Getter
public enum SortField {
    createdAt("createdAt"),
    rating("rating");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }
}
