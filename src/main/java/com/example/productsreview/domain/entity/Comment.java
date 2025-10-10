package com.example.productsreview.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private UUID commentId;
    private String customerId;
    private String customerName;
    private String parentCommentId;
    private String mentionedUserName;
    private String mentionedCustomerName;
    private String content;

    @CreatedDate
    private Instant createdAt = Instant.now();

    private Set<String> likedBy = new HashSet<>();
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