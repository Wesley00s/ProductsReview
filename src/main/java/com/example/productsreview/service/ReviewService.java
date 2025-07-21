package com.example.productsreview.service;

import com.example.productsreview.controller.dto.CommentSummaryResponse;
import com.example.productsreview.controller.dto.ReviewSummaryResponse;
import com.example.productsreview.domain.entity.Comment;
import com.example.productsreview.domain.entity.ReviewEntity;
import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongo;

    public ReviewService(ReviewRepository reviewRepository, MongoTemplate mongo) {
        this.reviewRepository = reviewRepository;
        this.mongo = mongo;
    }

    public void save(ReviewCreatedEvent event) {
        var entity = new ReviewEntity();
        entity.setReviewId(event.reviewId());
        entity.setProductId(event.productId());
        entity.setCustomerName(event.customerName());
        entity.setContent(event.content());
        entity.setCustomerId(event.customerId());
        entity.setRating(event.rating());

        reviewRepository.save(entity);
    }

    public Page<ReviewSummaryResponse> findSummariesByProductId(
            String productId,
            String sortField,
            Sort.Direction direction,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<ReviewEntity> reviewPage = reviewRepository.findAllByProductId(productId, pageable);

        return reviewPage.map(r -> new ReviewSummaryResponse(
                r.getReviewId(),
                r.getProductId(),
                r.getCustomerId(),
                r.getReviewLikes(),
                r.getReviewDislikes(),
                r.getCustomerName(),
                r.getContent(),
                r.getRating(),
                r.getTotalComments(),
                r.getCreatedAt()
        ));
    }

    public List<CommentSummaryResponse> findCommentsByReviewId(
            String reviewId,
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
                .map(c -> new CommentSummaryResponse(
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
                        c.getCreatedAt()
                ))
                .toList();
    }

    public List<CommentSummaryResponse> findReplies(String reviewId, String commentId) {

        Query q = Query.query(Criteria.where("_id").is(reviewId));
        q.fields().include("comments");
        ReviewEntity proj = mongo.findOne(q, ReviewEntity.class);
        if (proj == null) return List.of();

        Comment parent = findCommentRecursive(proj.getComments(), commentId);
        if (parent == null) return List.of();

        return parent.getReplies().stream()
                .map(c -> new CommentSummaryResponse(
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
                        c.getCreatedAt()
                ))
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

    public void toggleCommentLike(String reviewId, String commentId, String userId) {
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

    public void toggleCommentDislike(String reviewId, String commentId, String userId) {
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
        reviewRepository.deleteById(reviewId);
    }

    public void deleteComment(String reviewId, String commentId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            boolean removed = removeCommentRecursive(review.getComments(), commentId);
            if (removed) {
                reviewRepository.save(review);
            }
        });
    }

    private Comment findCommentRecursive(List<Comment> comments, String commentId) {
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

    private boolean removeCommentRecursive(List<Comment> comments, String commentId) {
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
