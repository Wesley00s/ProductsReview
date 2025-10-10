package com.example.productsreview.repository;

import com.example.productsreview.domain.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<ReviewEntity, String> {

    Page<ReviewEntity> findAllByProductCode(String productCode, Pageable pageable);

}
