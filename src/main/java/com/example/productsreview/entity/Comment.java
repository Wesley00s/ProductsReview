package com.example.productsreview.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    private String commentId;
    private String customerId;
    private String customerName;
    private String mentionedUserName;
    private String mentionedCustomerName;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    private int likes;
    private int dislikes;
    private String mentionedUserId;
    private List<Comment> replies = new ArrayList<>();
}