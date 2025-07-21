package com.example.productsreview.repository;

import com.example.productsreview.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<ReviewEntity, String> {

    Page<ReviewEntity> findAllByProductId(String productId, PageRequest pageRequest);

    Page<ReviewEntity> findAllByCustomerId(String customerId, PageRequest pageRequest);
}
