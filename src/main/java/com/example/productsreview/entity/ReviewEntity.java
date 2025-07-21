package com.example.productsreview.entity;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity {
    @MongoId
    private String reviewId;
    @Indexed(name = "product_id_index")
    private String productId;
    private String customerId;
    private String customerName;
    private String content;
    private int reviewLikes;
    private int reviewDislikes;
    @Field(targetType = FieldType.DOUBLE)
    private Double rating;

    @CreatedDate
    private LocalDateTime createdAt;
    private List<Comment> comments = new ArrayList<>();
}
