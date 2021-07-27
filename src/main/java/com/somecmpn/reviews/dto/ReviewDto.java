package com.somecmpn.reviews.dto;

import com.somecmpn.reviews.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

import static java.util.stream.Collectors.toList;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ReviewDto {
    private Long id;
    private long productId;
    private String text;
    private Integer rating;

    public static ReviewDto fromEntity(Review review) {
        return new ReviewDto(review.getId(), review.getProduct().getId(), review.getText(), review.getRating());
    }

    public static List<ReviewDto> fromEntities(List<Review> reviews) {
        return reviews.stream().map(ReviewDto::fromEntity).collect(toList());
    }
}
