package com.somecmpn.reviews.controller;

import com.somecmpn.reviews.dto.ReviewDto;
import com.somecmpn.reviews.dto.ReviewResponse;
import com.somecmpn.reviews.entity.Review;
import com.somecmpn.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.somecmpn.reviews.ProductReviewsApp.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PREFIX + "/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReviewController {

    private final ReviewService reviewService;

    @PutMapping
    public ResponseEntity<ReviewDto> update(@RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(ReviewDto.fromEntity(reviewService.save(reviewDto)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ReviewResponse> getReviewByProductId(@PathVariable long productId,
                                                               @RequestParam(name = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(reviewService.findReviewByProductSortedByRating(productId, page));
    }

    @PatchMapping("/{reviewId}/deactivate")
    public ResponseEntity<ReviewDto> deactivateReview(@PathVariable long reviewId) {
        return ResponseEntity.ok(ReviewDto.fromEntity(reviewService.removeReview(reviewId)));
    }
}
