package com.example.productsreview.controller.dto;

import java.util.List;

public record ApiResponse<T>(
        List<T> data,
        PaginationResponse pagination
) {
}
