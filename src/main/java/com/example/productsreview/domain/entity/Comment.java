package com.example.productsreview.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    private String commentId;
    private String customerId;
    private String customerName;
    private String parentCommentId;
    private String mentionedUserName;
    private String mentionedCustomerName;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();

    private Set<String> likedBy    = new HashSet<>();
    private Set<String> dislikedBy = new HashSet<>();

    private String mentionedUserId;
    private List<Comment> replies = new ArrayList<>();

    public Integer getTotalReplies() {
        return replies != null ? replies.size() : 0;
    }

    public Integer getCommentLikes() {
        return likedBy.size();
    }
    public Integer getCommentDislikes() {
        return dislikedBy.size();
    }
}