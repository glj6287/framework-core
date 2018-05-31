package com.hywin.framework.redis;

/**
 * Created by wuyouyang on 2017/5/17.
 */

public interface RedisService {
    void set(String var1, String var2);

    void set(String var1, String var2, long var3);

    void set(String key, Object value);

    String get(String var1);

    <T> T getObject(String key, Class<T> clazz);

    boolean containsKeyPattern(String var1);

    boolean hasKey(String var1);

    void del(String var1);
}
