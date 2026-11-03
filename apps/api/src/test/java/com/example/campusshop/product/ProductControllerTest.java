package com.example.campusshop.product;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.example.campusshop.common.GlobalExceptionHandler;
import com.example.campusshop.config.WebConfig;
import com.example.campusshop.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class, WebConfig.class})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=https://student.github.io",
        "app.cors.allowed-origin-patterns=https://*.preview.example"
})
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

    @Test
    void allowsCorsForConfiguredGithubPagesOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header(HttpHeaders.ORIGIN, "https://student.github.io")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://student.github.io"))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600"));
    }

    @Test
    void allowsCorsForMatchingOriginPattern() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header(HttpHeaders.ORIGIN, "https://demo.preview.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://demo.preview.example"));
    }

    @Test
    void rejectsCorsForUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header(HttpHeaders.ORIGIN, "https://unknown.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
