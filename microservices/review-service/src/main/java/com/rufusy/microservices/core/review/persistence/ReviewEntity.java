package com.rufusy.microservices.core.review.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")
})
public class ReviewEntity {
    @Id
    @GeneratedValue
    private Integer id;

    @Version
    private Integer version;

    private int productId;
    private int reviewId;
    private String author;
    private String subject;
    private String content;

    public ReviewEntity(int productId, int reviewId, String author, String subject, String content){
        this.productId = productId;
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
        this.content = content;
    }
}
