package com.somecmpn.reviews.dao;

import com.somecmpn.reviews.entity.Product;
import com.somecmpn.reviews.entity.Review;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static io.qala.datagen.RandomShortApi.*;
import static java.util.stream.Collectors.toList;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReviewRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void findsOnlySpecifiedProductsActiveReviews() {
        Product targetProduct = new Product().setTitle(alphanumeric(6));
        targetProduct = productRepository.save(targetProduct);
        Product other = new Product().setTitle(alphanumeric(6));
        other = productRepository.save(other);
        flushAndClear();

        Review review1 = new Review().setProduct(targetProduct).setRating(integer(1, 5)).setText(alphanumeric(50));
        review1 = reviewRepository.save(review1);
        Review review2 = new Review().setProduct(targetProduct).setRating(integer(1, 5)).setText(alphanumeric(50));
        review2 = reviewRepository.save(review2);
        Review removedReview = new Review().setProduct(targetProduct).setRating(integer(1, 5)).setText(alphanumeric(50)).setActive(false);
        reviewRepository.save(removedReview);
        reviewRepository.save(new Review().setProduct(other).setRating(integer(1, 5)).setText(alphanumeric(50)));
        flushAndClear();

        Page<Review> page = reviewRepository.findReviewsByActiveIsTrueAndProduct_Id(targetProduct.getId(), PageRequest.of(0, 4));
        List<Review> reviews = page.getContent();
        softly.assertThat(page.getTotalElements()).isEqualTo(2);
        softly.assertThat(reviews).size().isEqualTo(2);
        softly.assertThat(reviews.stream().map(Review::getId).collect(toList()))
                .contains(review1.getId(), review2.getId());
    }

    @Test
    public void returnsEmptyPage_whenThereIsNoReviews() {
        Product product = new Product().setTitle(alphanumeric(6));
        product = productRepository.save(product);
        flushAndClear();

        Page<Review> reviews = reviewRepository.findReviewsByActiveIsTrueAndProduct_Id(product.getId(), PageRequest.of(1, 5));
        softly.assertThat(reviews.getTotalElements()).isEqualTo(0);
        softly.assertThat(reviews.getContent()).size().isEqualTo(0);
    }

    @Test
    public void returnsEmptyPage_whenSpecifiedProductDoesNotExist() {
        Page<Review> reviews = reviewRepository.findReviewsByActiveIsTrueAndProduct_Id(positiveLong(), PageRequest.of(0, 5));
        softly.assertThat(reviews.getTotalElements()).isEqualTo(0);
        softly.assertThat(reviews.getContent()).size().isEqualTo(0);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
