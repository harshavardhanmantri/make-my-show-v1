package com.example.makemyshow.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value, long timeout, TimeUnit unit) {
        if (timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            // Default expiration for entries without specified timeout (1 day)
            redisTemplate.opsForValue().set(key, value, 1, TimeUnit.DAYS);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        return (T) value;
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void clearCache() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}