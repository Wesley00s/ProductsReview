package com.example.productsreview.scripts;

import com.example.productsreview.listener.dto.ReviewCreatedEvent;
import com.example.productsreview.listener.dto.CommentAddedEvent;
import com.example.productsreview.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(1)
public class BulkQueuePopulator implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;
    private final Random rnd = new Random();
    private int commentCounter = 1;

    public BulkQueuePopulator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) throws Exception {

        List<String> reviewIds = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String reviewId = UUID.randomUUID().toString();
            ReviewCreatedEvent rev = new ReviewCreatedEvent(
                    reviewId,
                    "prod-" + String.format("%03d", rnd.nextInt(200) + 1),
                    "user-" + String.format("%03d", rnd.nextInt(800) + 1),
                    "Cliente " + i,
                    "Comentário de review número " + i,
                    1.0 + rnd.nextDouble() * 4.0
            );
            rabbitTemplate.convertAndSend(RabbitMqConfig.REVIEW_CREATED_QUEUE, rev);
            reviewIds.add(reviewId);
        }


        Thread.sleep(2_000);


        for (String reviewId : reviewIds) {

            int topLevelCount = 1 + rnd.nextInt(10);
            List<CommentAddedEvent> allComments = new ArrayList<>();
            for (int c = 0; c < topLevelCount; c++) {
                buildCommentsRecursively(
                        reviewId,
                        null,
                        0,
                        allComments
                );
            }


            for (CommentAddedEvent cEvt : allComments) {
                rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_ADDED_QUEUE, cEvt);
            }
        }
    }

    private void buildCommentsRecursively(String reviewId,
                                          String parentId,
                                          int depth,
                                          List<CommentAddedEvent> collector) {

        String commentId = "cmt-" + (commentCounter++);
        CommentAddedEvent evt = new CommentAddedEvent(
                reviewId,
                commentId,
                parentId,
                "user-" + String.format("%03d", rnd.nextInt(800) + 1),
                "Usuário X",
                "Conteúdo do comentário #" + commentId,
                null,
                null
        );
        collector.add(evt);


        if (depth < 2) {
            int replies = rnd.nextInt(4);
            for (int i = 0; i < replies; i++) {
                buildCommentsRecursively(reviewId, commentId, depth + 1, collector);
            }
        }
    }
}
