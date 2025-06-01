package com.example.demo;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
    private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>("""
        local count = redis.call('INCR', KEYS[1])
        if count == 1 then redis.call('EXPIRE', KEYS[1], 60) end
        return count
        """, Long.class);

    private final StringRedisTemplate redis;
    private final int limit;

    RateLimiter(StringRedisTemplate redis, @Value("${demo.rate-limit}") int limit) {
        if (limit < 1) throw new IllegalArgumentException("Rate limit must be positive");
        this.redis = redis;
        this.limit = limit;
    }

    public boolean allow(String client) {
        String count = redis.execute(INCREMENT, List.of("demo:rate:" + client));
        return count != null && count <= limit;
    }
}
