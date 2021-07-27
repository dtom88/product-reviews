package com.somecmpn.reviews.dto;

import com.somecmpn.reviews.entity.Review;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewResponse {
    public static final int REVIEW_PAGE_SIZE = 5;

    private final long totalReviews;
    private final long totalPages;
    private final int currentPage;
    private final List<ReviewDto> reviews;

    public ReviewResponse(long totalReviews, int currPage, List<Review> reviews) {
        this.currentPage = currPage;
        this.totalReviews = totalReviews;
        this.totalPages = (long) Math.ceil(totalReviews / REVIEW_PAGE_SIZE);
        this.reviews = ReviewDto.fromEntities(reviews);
    }
}
