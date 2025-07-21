package com.example.productsreview.listener;

import com.example.productsreview.domain.entity.ReviewEntity;
import com.example.productsreview.domain.entity.Comment;
import com.example.productsreview.listener.dto.CommentAddedEvent;
import com.example.productsreview.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.example.productsreview.config.RabbitMqConfig.COMMENT_ADDED_QUEUE;

@Component
public class CommentAddedListener {
    private final Logger logger = LoggerFactory.getLogger(CommentAddedListener.class);

    private final ReviewRepository reviewRepository;

    public CommentAddedListener(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @RabbitListener(queues = COMMENT_ADDED_QUEUE)
    public void handleCommentAddedEvent(Message<CommentAddedEvent> message) {
        CommentAddedEvent event = message.getPayload();
        logger.info("Received CommentAddedEvent: {}", event);

        Optional<ReviewEntity> reviewOpt = reviewRepository.findById(event.reviewId());

        if (reviewOpt.isEmpty()) {
            logger.error("Review not found for ID: {}", event.reviewId());
            return;
        }

        ReviewEntity review = reviewOpt.get();
        addCommentToReview(review, event);
        reviewRepository.save(review);
        logger.info("Added comment {} to review {}", event.commentId(), event.reviewId());
    }

    private void addCommentToReview(ReviewEntity review, CommentAddedEvent event) {
        Comment newComment = new Comment();
        newComment.setCommentId(event.commentId());
        newComment.setCustomerId(event.customerId());
        newComment.setCustomerName(event.customerName());
        newComment.setContent(event.content());
        newComment.setMentionedUserId(event.mentionedUserId());
        newComment.setMentionedUserName(event.mentionedUserName());
        newComment.setLikedBy(new HashSet<>());
        newComment.setDislikedBy(new HashSet<>());
        newComment.setReplies(new ArrayList<>());
        newComment.setCreatedAt(LocalDateTime.now());

        if (event.parentCommentId() == null) {
            review.getComments().add(newComment);
        }
        else {
            Comment parent = findCommentRecursive(
                    review.getComments(),
                    event.parentCommentId()
            );

            if (parent != null) {
                parent.getReplies().add(newComment);
            } else {
                logger.warn("Parent comment {} not found. Adding as top-level comment", event.parentCommentId());
                review.getComments().add(newComment);
            }
        }
    }

    private Comment findCommentRecursive(List<Comment> comments, String commentId) {
        for (Comment comment : comments) {
            if (comment.getCommentId().equals(commentId)) {
                return comment;
            }

            Comment found = findCommentRecursive(comment.getReplies(), commentId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}