package com.somecmpn.reviews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.somecmpn.reviews.dto.ReviewDto;
import com.somecmpn.reviews.entity.Product;
import com.somecmpn.reviews.entity.Review;
import com.somecmpn.reviews.service.ReviewService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityNotFoundException;

import static com.somecmpn.reviews.utils.ErrorMessageUtils.getEntityNotFoundMessage;
import static io.qala.datagen.RandomShortApi.positiveLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {
    @MockBean
    private ReviewService service;
    @Autowired
    private MockMvc mvc;
    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void postsNewReview() throws Exception {
        long id = positiveLong();
        ReviewDto dto = new ReviewDto().setProductId(positiveLong());
        Mockito.when(service.save(Mockito.any(ReviewDto.class)))
                .thenReturn(new Review().setId(id).setProduct(new Product().setId(dto.getProductId())));

        mvc.perform(MockMvcRequestBuilders
                .put("/api/v1/reviews")
                .content(mapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id));
    }

    @Test
    public void errs_whenEditReview() throws Exception {
        long id = positiveLong();
        Mockito.doThrow(new EntityNotFoundException(getEntityNotFoundMessage(Product.class, id)))
                .when(service).save(Mockito.any(ReviewDto.class));

        mvc.perform(MockMvcRequestBuilders
                .put("/api/v1/reviews")
                .content(mapper.writeValueAsString(new ReviewDto()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deactivatesExistingReview() throws Exception {
        long id = positiveLong();
        Mockito.when(service.removeReview(Mockito.anyLong()))
                .thenReturn(new Review().setId(id).setProduct(new Product().setId(positiveLong())).setActive(false));

        mvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/reviews/{id}/deactivate", id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id));
    }

    @Test
    public void errs_whenTryingToDeactivateNonExistingReview() throws Exception {
        long id = positiveLong();
        Mockito.doThrow(new EntityNotFoundException(getEntityNotFoundMessage(Review.class, id)))
                .when(service).removeReview(Mockito.anyLong());

        mvc.perform(MockMvcRequestBuilders
                .patch("/api/v1/reviews/{id}/deactivate", id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
