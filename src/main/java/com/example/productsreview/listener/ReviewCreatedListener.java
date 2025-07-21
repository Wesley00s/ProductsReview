package com.example.productsreview.listener;

import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static com.example.productsreview.config.RabbitMqConfig.REVIEW_CREATED_QUEUE;

@Component
public class ReviewCreatedListener {
    private final Logger logger = LoggerFactory.getLogger(ReviewCreatedListener.class);
    private final ReviewService reviewService;
    public ReviewCreatedListener(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RabbitListener(queues = REVIEW_CREATED_QUEUE)
    public void listen(Message<ReviewCreatedEvent> message) {
        logger.info("Received message: {}", message.getPayload());

        logger.info("Processing review created event for review ID: {}", message.getPayload().reviewId());
        reviewService.save(message.getPayload());
    }
}
