package com.somecmpn.reviews.entity;

import com.somecmpn.reviews.dto.ReviewDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter @Getter
@Accessors(chain = true)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rating;
    private String text;
    @ManyToOne
    @JoinColumn(name="product")
    private Product product;
    private boolean active = true;

    public Review(ReviewDto dto, Product product) {
        this.product = product;
        this.rating = dto.getRating();
        this.text = dto.getText();
    }

}
