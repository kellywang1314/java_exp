package com.example.javaexp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import com.example.javaexp.product.api.ProductResponse;

class CacheConfigTest {

    @Test
    void serializesProductResponseWithJavaTime() {
        GenericJackson2JsonRedisSerializer serializer = CacheConfig.createRedisSerializer();
        LocalDateTime now = LocalDateTime.of(2026, 10, 25, 10, 0);
        ProductResponse product = new ProductResponse(
                1L,
                "Hotpot Set",
                "Group Meal",
                new BigDecimal("128.00"),
                "A hotpot meal for two people",
                now,
                now);

        Object restored = serializer.deserialize(serializer.serialize(product));

        assertThat(restored).isEqualTo(product);
    }
}
