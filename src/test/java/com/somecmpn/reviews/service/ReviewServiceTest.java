package com.somecmpn.reviews.service;

import com.somecmpn.reviews.dao.ProductRepository;
import com.somecmpn.reviews.dto.ReviewDto;
import com.somecmpn.reviews.entity.Product;
import com.somecmpn.reviews.entity.Review;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import java.util.List;

import static io.qala.datagen.RandomShortApi.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ReviewServiceTest {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager entityManager;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void errs_whenTryingToCreateReviewOfNonExistingProduct() {
        long randomId = positiveLong();
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> reviewService.save(new ReviewDto().setProductId(randomId)));
        assertEquals(String.format("Product with id = %s not found.", randomId), thrown.getMessage());
    }

    @Test
    public void savesNewReview() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        ReviewDto dto = new ReviewDto()
                .setProductId(product.getId())
                .setRating(integer(0, 5))
                .setText(alphanumeric(40));
        Review saved = reviewService.save(dto);
        softly.assertThat(saved.getId()).isNotNull();
        flushAndClear();

        Review fromDb = reviewService.findById(saved.getId());
        softly.assertThat(fromDb.getRating()).isEqualTo(dto.getRating());
        softly.assertThat(fromDb.getText()).isEqualTo(dto.getText());
        softly.assertThat(fromDb.getProduct().getId()).isEqualTo(dto.getProductId());
    }

    @Test
    public void savesExistingReviewUpdate() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        String initialText = alphanumeric(40);
        ReviewDto dto = new ReviewDto()
                .setProductId(product.getId())
                .setRating(integer(0, 5))
                .setText(initialText);
        reviewService.save(dto);
        flushAndClear();

        dto.setText(alphanumeric(10));
        Review updated = reviewService.save(dto);
        flushAndClear();

        Review fromDb = reviewService.findById(updated.getId());
        softly.assertThat(fromDb.getText()).isEqualTo(dto.getText());
        softly.assertThat(fromDb.getText()).isNotEqualTo(initialText);
    }

    @Test
    public void errs_whenTryingToSaveNonExistingReviewUpdate() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        long randomId = positiveLong();
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> reviewService.save(new ReviewDto().setId(randomId).setProductId(product.getId())));
        assertEquals(String.format("Review with id = %s not found.", randomId), thrown.getMessage());
    }

    @Test
    public void errs_whenTryingToDeactivateNonExistingReview() {
        long randomId = positiveLong();
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> reviewService.removeReview(randomId));
        assertEquals(String.format("Review with id = %s not found.", randomId), thrown.getMessage());
    }

    @Test
    public void deactivatesExistingReview() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        ReviewDto dto = new ReviewDto()
                .setProductId(product.getId())
                .setRating(integer(0, 5))
                .setText(alphanumeric(40));
        Review saved = reviewService.save(dto);
        flushAndClear();

        Review deactivated = reviewService.removeReview(saved.getId());
        softly.assertThat(deactivated.isActive()).isFalse();
        flushAndClear();

        Review fromDb = reviewService.findById(saved.getId());
        softly.assertThat(fromDb.isActive()).isFalse();
    }

    @Test
    public void errs_whenTryingToFindReviewsOfNonExistingProduct() {
        long randomId = positiveLong();
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> reviewService.findReviewByProductSortedByRating(randomId, 0));
        assertEquals(String.format("Product with id = %s not found.", randomId), thrown.getMessage());
    }

    @Test
    public void returnsReviewsSortedByRating_whenFindingByProductId() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        Review lowRatingReview = reviewService.save(new ReviewDto().setRating(3).setProductId(product.getId()).setText(alphanumeric(40)));
        Review highRatingReview = reviewService.save(new ReviewDto().setRating(5).setProductId(product.getId()).setText(alphanumeric(40)));
        flushAndClear();

        List<ReviewDto> reviews = reviewService.findReviewByProductSortedByRating(product.getId(), 0).getReviews();
        softly.assertThat(reviews).size().isEqualTo(2);
        softly.assertThat(reviews.get(0).getId()).isEqualTo(highRatingReview.getId());
        softly.assertThat(reviews.get(1).getId()).isEqualTo(lowRatingReview.getId());
    }

    @Test
    public void returnsMaxFiveReviewsPerPage_whenFindingByProductId() {
        Product product = productRepository.save(new Product().setTitle(alphanumeric(6)));
        flushAndClear();

        for (int i = 0; i < 6; i ++)
            reviewService.save(new ReviewDto().setRating(integer(0, 5)).setProductId(product.getId()).setText(alphanumeric(40)));
        flushAndClear();

        List<ReviewDto> reviews = reviewService.findReviewByProductSortedByRating(product.getId(), 0).getReviews();
        softly.assertThat(reviews).size().isEqualTo(5);

        reviews = reviewService.findReviewByProductSortedByRating(product.getId(), 1).getReviews();
        softly.assertThat(reviews).size().isEqualTo(1);

        reviews = reviewService.findReviewByProductSortedByRating(product.getId(), 2).getReviews();
        softly.assertThat(reviews).size().isEqualTo(0);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
