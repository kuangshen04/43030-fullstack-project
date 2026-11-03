package com.example.campusshop.product;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.example.campusshop.common.GlobalExceptionHandler;
import com.example.campusshop.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    private static final ProductResponse SAMPLE_PRODUCT = new ProductResponse(
            1L,
            "Aurora Mechanical Keyboard",
            "A compact keyboard.",
            new BigDecimal("89.00"),
            12,
            "Electronics",
            OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC),
            OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void listsProducts() throws Exception {
        when(productService.findAll(null, null)).thenReturn(List.of(SAMPLE_PRODUCT));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aurora Mechanical Keyboard"))
                .andExpect(jsonPath("$[0].price").value(89.00))
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }

    @Test
    void returnsNotFoundForMissingProduct() throws Exception {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        String body = """
                {
                  "name": " ",
                  "description": "Invalid product",
                  "price": -1,
                  "stock": 2,
                  "category": "Electronics"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deletesProductsWithNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
