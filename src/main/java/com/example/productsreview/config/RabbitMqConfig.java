package com.example.productsreview.config;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REVIEW_CREATED_QUEUE = "review.created.queue";
    public static final String COMMENT_ADDED_QUEUE = "comment.added.queue";

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Declarable reviewCreatedQueue() {
        return new Queue(REVIEW_CREATED_QUEUE);
    }

    @Bean
    public Declarable commentAddedQueue() {
        return new Queue(COMMENT_ADDED_QUEUE);
    }
}
