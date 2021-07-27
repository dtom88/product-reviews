package com.somecmpn.reviews.service;

import com.somecmpn.reviews.dao.ProductRepository;
import com.somecmpn.reviews.dao.ReviewRepository;
import com.somecmpn.reviews.dto.ReviewDto;
import com.somecmpn.reviews.dto.ReviewResponse;
import com.somecmpn.reviews.entity.Product;
import com.somecmpn.reviews.entity.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import static com.somecmpn.reviews.utils.ErrorMessageUtils.getEntityNotFoundMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ReviewResponse findReviewByProductSortedByRating(Long productId, int currentPage) {
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(getEntityNotFoundMessage(Product.class, productId)));

        Page<Review> reviews = reviewRepository.findReviewsByActiveIsTrueAndProduct_Id(productId,
                PageRequest.of(currentPage, ReviewResponse.REVIEW_PAGE_SIZE, Sort.by("rating").descending()));
        return new ReviewResponse(reviews.getTotalElements(), currentPage, reviews.getContent());
    }

    @Transactional(readOnly = true)
    public Review findById(long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityNotFoundMessage(Review.class, id)));
    }

    @Transactional
    public Review save(ReviewDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(getEntityNotFoundMessage(Product.class, dto.getProductId())));

        Review review = dto.getId() == null
                ? new Review().setProduct(product)
                : reviewRepository.findById(dto.getId()).orElseThrow(
                        () -> new EntityNotFoundException(getEntityNotFoundMessage(Review.class, dto.getId())));
        review.setRating(dto.getRating()).setText(dto.getText());
        return reviewRepository.save(review);
    }

    @Transactional
    public Review removeReview(long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityNotFoundMessage(Review.class, id)));

        return reviewRepository.save(review.setActive(false));
    }
}
