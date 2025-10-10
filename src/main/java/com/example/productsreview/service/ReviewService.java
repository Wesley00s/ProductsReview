package com.example.productsreview.service;

import com.example.productsreview.api.v1.controller.dto.CommentSummaryResponse;
import com.example.productsreview.api.v1.controller.dto.ProductRatingUpdatedEvent;
import com.example.productsreview.api.v1.controller.dto.ReviewSummaryResponse;
import com.example.productsreview.domain.entity.Comment;
import com.example.productsreview.domain.entity.RatingAggregationResult;
import com.example.productsreview.domain.entity.ReviewEntity;
import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.repository.ReviewRepository;
import com.example.productsreview.config.RabbitMqConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongo;
    private final RabbitTemplate rabbitTemplate;

    public void save(ReviewCreatedEvent event) {
        var entity = new ReviewEntity();
        entity.setReviewId(event.reviewId());
        entity.setProductId(event.productId());
        entity.setProductCode(event.productCode());
        entity.setCustomerName(event.customerName());
        entity.setContent(event.content());
        entity.setCustomerId(event.customerId());
        entity.setRating(event.rating());
        entity.setCreatedAt(Instant.now());

        reviewRepository.save(entity);

        calculateAndPublishAverageRating(event.productCode());
    }

    private void calculateAndPublishAverageRating(String productCode) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("productCode").is(productCode));

        GroupOperation groupStage = Aggregation.group("productCode")
                .avg("rating").as("averageRating")
                .count().as("totalReviews");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);

        AggregationResults<RatingAggregationResult> results = mongo.aggregate(
                aggregation,
                "reviews",
                RatingAggregationResult.class
        );

        RatingAggregationResult result = results.getUniqueMappedResult();

        Double average = 0.0;
        Long total = 0L;

        if (result != null) {
            average = result.getAverageRating() != null ? result.getAverageRating() : 0.0;
            total = result.getTotalReviews() != null ? result.getTotalReviews() : 0L;
        }

        var event = new ProductRatingUpdatedEvent(average, total, productCode);
        rabbitTemplate.convertAndSend(RabbitMqConfig.PRODUCT_RATING_UPDATED_QUEUE, event);
    }

    public Page<ReviewSummaryResponse> findSummariesByProductCode(
            String productCode,
            String currentUserId,
            String sortField,
            Sort.Direction direction,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<ReviewEntity> reviewPage = reviewRepository.findAllByProductCode(productCode, pageable);

        return reviewPage.map(r -> {
            Boolean userHasLiked = (currentUserId != null) && r.getLikedBy().contains(currentUserId);
            Boolean userHasDisliked = (currentUserId != null) && r.getDislikedBy().contains(currentUserId);

            return new ReviewSummaryResponse(
                    r.getReviewId(),
                    r.getProductId(),
                    r.getProductCode(),
                    r.getCustomerId(),
                    r.getReviewLikes(),
                    r.getReviewDislikes(),
                    r.getCustomerName(),
                    r.getContent(),
                    r.getRating(),
                    r.getTotalComments(),
                    r.getCreatedAt(),
                    userHasLiked,
                    userHasDisliked
            );
        });
    }

    public List<CommentSummaryResponse> findCommentsByReviewId(
            String reviewId,
            String currentUserId,
            Integer skip,
            Integer limit
    ) {
        Query q = Query.query(Criteria.where("_id").is(reviewId));
        q.fields().include("comments");
        q.fields().slice("comments", skip, limit);

        ReviewEntity proj = mongo.findOne(q, ReviewEntity.class);
        if (proj == null || proj.getComments() == null) {
            return List.of();
        }


        return proj.getComments().stream()
                .map(c -> {
                            Boolean userHasLiked = (currentUserId != null) && c.getLikedBy().contains(currentUserId);
                            Boolean userHasDisliked = (currentUserId != null) && c.getDislikedBy().contains(currentUserId);
                            return new CommentSummaryResponse(
                                    c.getCommentId(),
                                    c.getCustomerId(),
                                    c.getCustomerName(),
                                    c.getContent(),
                                    null,
                                    c.getMentionedUserId(),
                                    c.getMentionedUserName(),
                                    c.getCommentLikes(),
                                    c.getCommentDislikes(),
                                    c.getReplies() != null ? c.getReplies().size() : 0,
                                    c.getCreatedAt(),
                                    userHasLiked,
                                    userHasDisliked
                            );
                        }
                )
                .toList();
    }

    public List<CommentSummaryResponse> findReplies(
            String reviewId,
            String currentUserId,
            UUID commentId
    ) {

        Query q = Query.query(Criteria.where("_id").is(reviewId));
        q.fields().include("comments");
        ReviewEntity proj = mongo.findOne(q, ReviewEntity.class);
        if (proj == null) return List.of();

        Comment parent = findCommentRecursive(proj.getComments(), commentId);
        if (parent == null) return List.of();

        return parent.getReplies().stream()
                .map(c -> {
                            Boolean userHasLiked = (currentUserId != null) && c.getLikedBy().contains(currentUserId);
                            Boolean userHasDisliked = (currentUserId != null) && c.getDislikedBy().contains(currentUserId);
                            return new CommentSummaryResponse(
                                    c.getCommentId(),
                                    c.getCustomerId(),
                                    c.getCustomerName(),
                                    c.getContent(),
                                    parent.getCommentId(),
                                    c.getMentionedUserId(),
                                    c.getMentionedUserName(),
                                    c.getCommentLikes(),
                                    c.getCommentDislikes(),
                                    c.getTotalReplies(),
                                    c.getCreatedAt(),
                                    userHasLiked,
                                    userHasDisliked
                            );
                        }
                )
                .toList();
    }

    public void toggleLike(String reviewId, String userId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            if (!review.getLikedBy().remove(userId)) {
                review.getLikedBy().add(userId);
                review.getDislikedBy().remove(userId);
            }
            reviewRepository.save(review);
        });
    }

    public void toggleDislike(String reviewId, String userId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            if (!review.getDislikedBy().remove(userId)) {
                review.getDislikedBy().add(userId);
                review.getLikedBy().remove(userId);
            }
            reviewRepository.save(review);
        });
    }

    public void toggleCommentLike(String reviewId, UUID commentId, String userId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            Comment comment = findCommentRecursive(review.getComments(), commentId);
            if (comment != null) {
                if (!comment.getLikedBy().remove(userId)) {
                    comment.getLikedBy().add(userId);
                    comment.getDislikedBy().remove(userId);
                }
                reviewRepository.save(review);
            }
        });
    }

    public void toggleCommentDislike(String reviewId, UUID commentId, String userId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            Comment comment = findCommentRecursive(review.getComments(), commentId);
            if (comment != null) {
                if (!comment.getDislikedBy().remove(userId)) {
                    comment.getDislikedBy().add(userId);
                    comment.getLikedBy().remove(userId);
                }
                reviewRepository.save(review);
            }
        });
    }

    public void deleteReview(String reviewId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            reviewRepository.deleteById(reviewId);
            calculateAndPublishAverageRating(review.getProductCode());
        });
    }

    public void deleteComment(String reviewId, UUID commentId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            boolean removed = removeCommentRecursive(review.getComments(), commentId);
            if (removed) {
                reviewRepository.save(review);
            }
        });
    }

    private Comment findCommentRecursive(List<Comment> comments, UUID commentId) {
        for (Comment c : comments) {
            if (c.getCommentId().equals(commentId)) {
                return c;
            }
            Comment found = findCommentRecursive(c.getReplies(), commentId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private boolean removeCommentRecursive(List<Comment> comments, UUID commentId) {
        if (comments.removeIf(c -> c.getCommentId().equals(commentId))) {
            return true;
        }
        for (Comment c : comments) {
            if (removeCommentRecursive(c.getReplies(), commentId)) {
                return true;
            }
        }
        return false;
    }
}
