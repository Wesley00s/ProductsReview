package com.example.productsreview.listener.dto;

public record AuthenticatedUser(
        String id,
        String userName,
        String email
) {
}