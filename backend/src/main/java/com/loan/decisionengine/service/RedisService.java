package com.loan.decisionengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public void expire(String key, long ttlSeconds) {
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    public void cacheDecision(Long applicationId, Object decision) {
        set("decision:" + applicationId, decision, 86400);
    }

    public Object getCachedDecision(Long applicationId) {
        return get("decision:" + applicationId);
    }

    public void evictDecision(Long applicationId) {
        delete("decision:" + applicationId);
    }

    public void cacheApplicationStatus(String appNumber, String status) {
        set("app:status:" + appNumber, status, 86400);
    }

    public String getCachedApplicationStatus(String appNumber) {
        Object v = get("app:status:" + appNumber);
        return v != null ? v.toString() : null;
    }
}
