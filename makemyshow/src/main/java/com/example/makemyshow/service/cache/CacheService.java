package com.example.makemyshow.service.cache;

import java.util.concurrent.TimeUnit;

public interface CacheService {
    void put(String key, Object value, long timeout, TimeUnit unit);
    <T> T get(String key, Class<T> type);
    void delete(String key);
    boolean hasKey(String key);
    void clearCache();
}