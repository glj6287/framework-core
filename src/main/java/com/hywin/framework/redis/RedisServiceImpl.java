package com.hywin.framework.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyouyang on 2017/5/17.
 */

public class RedisServiceImpl implements RedisService {
    public static final int EXPIRED_TIME = 54000;
    private RedisTemplate template;
    private String namespace;

    public RedisTemplate getTemplate() {
        return this.template;
    }

    public void setTemplate(RedisTemplate template) {
        this.template = template;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private String queryKey(String key) {
        if(StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Redis key is null!");
        }

        if(StringUtils.isEmpty(this.getNamespace())) {
            throw new IllegalArgumentException("需要指定redis.namespace");
        }

        return this.getNamespace() + "_" + key;
    }

    public void set(String key, String value) {
        key = this.queryKey(key);
        ValueOperations ops = this.template.opsForValue();
        ops.set(key, value);
    }

    public void set(String key, String value, long expired) {
        key = this.queryKey(key);
        ValueOperations ops = this.template.opsForValue();
        ops.set(key, value, expired, TimeUnit.SECONDS);
    }

    public void set(String key, Object value) {
        if(value==null) {
            throw new IllegalArgumentException("Object is null!");
        }
        key = this.queryKey(key);
        ValueOperations ops = this.template.opsForValue();
        ops.set(key, JSON.toJSONString(value));
    }

    public <T> T getObject(String key, Class<T> clazz) {
        key = this.queryKey(key);
        ValueOperations ops = this.template.opsForValue();
        return JSONObject.parseObject((String)ops.get(key), clazz);
    }

    public String get(String key) {
        key = this.queryKey(key);
        ValueOperations ops = this.template.opsForValue();
        return (String)ops.get(key);
    }

    public boolean containsKeyPattern(String key) {
        key = this.queryKey(key);
        Set keys = this.template.keys(key + "*");
        return !CollectionUtils.isEmpty(keys);
    }

    public boolean hasKey(String key) {
        key = this.queryKey(key);
        return this.template.hasKey(key).booleanValue();
    }

    public void del(String key) {
        key = this.queryKey(key);
        this.template.delete(key);
    }
}
