package com.example.productsreview.config;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REVIEW_CREATED_QUEUE = "review.created.queue";
    public static final String REVIEW_LIKED_QUEUE = "review.liked.queue";
    public static final String REVIEW_DISLIKED_QUEUE = "review.disliked.queue";
    public static final String COMMENT_ADDED_QUEUE = "comment.added.queue";
    public static final String COMMENT_LIKED_QUEUE = "comment.liked.queue";
    public static final String COMMENT_DISLIKED_QUEUE = "comment.disliked.queue";


    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Declarable reviewCreatedQueue() {
        return new Queue(REVIEW_CREATED_QUEUE);
    }

    @Bean
    public Declarable reviewLikedQueue() {
        return new Queue(REVIEW_LIKED_QUEUE);
    }

    @Bean
    public Declarable reviewDislikedQueue() {
        return new Queue(REVIEW_DISLIKED_QUEUE);
    }

    @Bean
    public Declarable commentAddedQueue() {
        return new Queue(COMMENT_ADDED_QUEUE);
    }

    @Bean
    public Queue commentLikedQueue() {
        return new Queue(COMMENT_LIKED_QUEUE);
    }

    @Bean
    public Queue commentDislikedQueue() {
        return new Queue(COMMENT_DISLIKED_QUEUE);
    }

}
