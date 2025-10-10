package com.example.productsreview.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.*;

@Document(collection = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity {
    @MongoId
    private UUID reviewId;
    @Indexed(name = "product_id_index")
    private Long productId;
    private String productCode;
    private String customerId;
    private String customerName;
    private String content;

    private Set<String> likedBy = new HashSet<>();
    private Set<String> dislikedBy = new HashSet<>();

    @Field(targetType = FieldType.DOUBLE)
    private Double rating;

    @CreatedDate
    private Instant createdAt = Instant.now();
    private List<Comment> comments = new ArrayList<>();

    public Integer getTotalComments() {
        return comments != null ? comments.size() : 0;
    }

    public Integer getReviewLikes() {
        return likedBy.size();
    }

    public Integer getReviewDislikes() {
        return dislikedBy.size();
    }

}
