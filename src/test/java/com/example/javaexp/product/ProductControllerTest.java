package com.example.javaexp.product;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.javaexp.product.api.ProductRequest;
import com.example.javaexp.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearDatabase() {
        productRepository.deleteAll();
        cacheManager.getCache("products").clear();
    }

    @Test
    void createsAndReadsProduct() throws Exception {
        ProductRequest request = new ProductRequest(
                "Hotpot Set",
                "Group Meal",
                new BigDecimal("128.00"),
                "A hotpot meal for two people");

        String responseBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hotpot Set"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(128.00));
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        ProductRequest request = new ProductRequest(
                "",
                "Group Meal",
                BigDecimal.ZERO,
                "");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    void executesNativeSqlQuery() throws Exception {
        createProduct("Budget Meal", "68.00");
        createProduct("Premium Meal", "168.00");

        mockMvc.perform(get("/api/products/price-above")
                        .param("minPrice", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Premium Meal"));
    }

    @Test
    void pagesAndSearchesProducts() throws Exception {
        createProduct("Searchable Hotpot", "128.00");
        createProduct("Searchable Barbecue", "158.00");
        createProduct("Coffee", "28.00");

        mockMvc.perform(get("/api/products/page")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void updatesCacheAndRemovesDeletedProduct() throws Exception {
        Long productId = createProduct("Original Meal", "88.00");
        ProductRequest updateRequest = new ProductRequest(
                "Updated Meal",
                "Group Meal",
                new BigDecimal("98.00"),
                "Updated description");

        mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Meal"));

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Meal"));

        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsRequestIdHeader() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("X-Request-Id", "learning-request-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "learning-request-001"));
    }

    private Long createProduct(String name, String price) throws Exception {
        ProductRequest request = new ProductRequest(
                name,
                "Group Meal",
                new BigDecimal(price),
                name + " description");

        String responseBody = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
