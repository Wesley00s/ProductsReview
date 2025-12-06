# ⭐ Product Reviews Microservice

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

This repository contains the **Reviews & Ratings Service** for the E-commerce ecosystem. It is a specialized microservice built to handle high-volume user feedback, comments, and interactions (likes/dislikes) using a non-relational database for scalability and flexibility.

## 🚀 Key Features

* **Review Management**: Users can create, update, and list reviews for products.
* **Interaction System**: Support for complex user interactions:
    * **Comments**: Nested discussions on specific reviews.
    * **Reactions**: Like and Dislike functionality for both Reviews and Comments.
* **Aggregations**: Efficient calculation of product rating averages and summaries using MongoDB Aggregation Framework.
* **Event-Driven Updates**: Utilizes RabbitMQ to handle reaction events asynchronously (e.g., updating counters when a review is liked).
* **Reactive Security**: JWT-based stateless authentication.

## 🛠️ Tech Stack

* **Language**: Java 21
* **Framework**: Spring Boot 3.5.3
* **Database**: MongoDB (NoSQL)
* **Messaging**: RabbitMQ
* **Documentation**: OpenAPI (Swagger UI)
* **Build Tool**: Gradle
* **Observability**: Prometheus & Micrometer

## 🏗️ Architecture

This service follows a **Hexagonal/Clean Architecture** approach with a strong focus on asynchronous processing.

```text
src/main/java/com/example/productsreview/
├── api/            # REST Controllers (Entry points)
├── domain/         # Entities (Review, Comment) and Business Logic
├── listener/       # RabbitMQ Listeners (Event handlers)
├── repository/     # MongoDB Data Access Layer
└── service/        # Business Use Cases
```

### Event System
The service relies on RabbitMQ to decouple high-frequency interactions.
* **Listeners**: `ReviewReactionListener`, `CommentReactionListener`.
* **Events**: `ReviewLikedEvent`, `CommentAddedEvent`, `ProductRatingUpdatedEvent`.

## ⚙️ Environment Configuration

Create a `.env` file in the root directory. You can use `.env.example` as a template:

```env
# Authentication
ISSUER=your_auth_issuer_url

# MongoDB
MONGO_INITDB_ROOT_USERNAME=mongo
MONGO_INITDB_ROOT_PASSWORD=secret
SPRING_DATA_MONGODB_HOST=mongodb
SPRING_DATA_MONGODB_PORT=27017
SPRING_DATA_MONGODB_DATABASE=review_db

# RabbitMQ
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
```

> **Note:** Place your RSA keys (`private_key.pem` and `public_key.pem`) in `src/main/resources/certs/` to enable JWT verification.

## 🐳 Running with Docker

This service is designed to run within the larger E-commerce docker network.

1.  **Ensure the shared network exists:**
    ```bash
    docker network create shared_ecommerce_network
    ```

2.  **Start the service:**
    ```bash
    docker-compose up -d --build
    ```

3.  **Access the Endpoints:**
    * **API Base**: `http://localhost:8081/reviews/api`
    * **Swagger Docs**: `http://localhost:8081/reviews/api/docs`
    * **Prometheus Metrics**: `http://localhost:8081/reviews/api/actuator/prometheus`

## 📦 Manual Installation (Gradle)

If you prefer to run it locally without Docker (requires local MongoDB and RabbitMQ instances):

1.  **Build:**
    ```bash
    ./gradlew clean build -x test
    ```

2.  **Run:**
    ```bash
    java -jar build/libs/app.jar
    ```

## 📄 API Documentation

The API follows RESTful standards. You can explore the endpoints via Swagger UI running at `/reviews/api/docs`.

**Key Endpoints:**
* `POST /reviews`: Create a review.
* `GET /reviews/product/{productId}`: Get reviews for a product.
* `POST /reviews/{reviewId}/comments`: Add a comment.
* `POST /reviews/{reviewId}/react`: Like/Dislike a review.

## 🤝 Contributing

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/NewFeature`).
3.  Commit your changes.
4.  Push to the branch.
5.  Open a Pull Request.
