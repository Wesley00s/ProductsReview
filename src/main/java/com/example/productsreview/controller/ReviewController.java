package com.example.productsreview.controller;

import com.example.productsreview.config.RabbitMqConfig;
import com.example.productsreview.controller.dto.*;
import com.example.productsreview.listener.dto.CommentAddedEvent;
import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.service.ReviewService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final RabbitTemplate rabbitTemplate;


    public ReviewController(ReviewService reviewService, RabbitTemplate rabbitTemplate) {
        this.reviewService = reviewService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewsByProductId(
            @PathVariable String productId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        var reviews = reviewService.findAllByProductId(productId, page, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        reviews.getContent(),
                        new PaginationResponse(
                                reviews.getNumber(),
                                reviews.getSize(),
                                reviews.getTotalElements(),
                                reviews.getTotalPages()
                        )
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewsByCustomerId(
            @PathVariable String customerId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        var reviews = reviewService.findAllByCustomerId(customerId, page, pageSize);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        reviews.getContent(),
                        new PaginationResponse(
                                reviews.getNumber(),
                                reviews.getSize(),
                                reviews.getTotalElements(),
                                reviews.getTotalPages()
                        )
                )
        );
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody CreateReviewRequest request
    ) {
        ReviewCreatedEvent event = new ReviewCreatedEvent(
                request.reviewId(),
                request.productId(),
                request.customerId(),
                request.customerName(),
                request.content(),
                request.rating()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.REVIEW_CREATED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + request.reviewId())
        ).body(
                new ReviewResponse(
                        request.reviewId(),
                        request.productId(),
                        request.customerId(),
                        request.customerName(),
                        request.content(),
                        new ArrayList<>(),
                        request.rating()
                )
        );
    }


    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String reviewId,
            @RequestBody CreateCommentRequest request
    ) {
        CommentAddedEvent event = new CommentAddedEvent(
                reviewId,
                request.commentId(),
                request.parentCommentId(),
                request.customerId(),
                request.customerName(),
                request.content(),
                request.mentionedUserId(),
                request.mentionedUserName()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_ADDED_QUEUE, event);

        return ResponseEntity.created(
                URI.create("/reviews/" + reviewId + "/comments/" + request.commentId())
        ).body(
                new CommentResponse(
                        request.commentId(),
                        request.customerId(),
                        request.customerName(),
                        request.content(),
                        new ArrayList<>()
                )
        );
    }
}
