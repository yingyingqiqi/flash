package com.yingqi.flash.redis;

import com.alibaba.fastjson.JSON;
import com.yingqi.flash.domain.FlashUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    /**
     * 获取单个对象
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix,String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = jedis.get(prefix.getPrefix()+key);
            T t = StringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    @SuppressWarnings("cunchecked")
    public static <T> T StringToBean(String str,Class<T> clazz) {
        if (str == null || str.length() < 0) {
            return null;
        } if (clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T)str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        }else{
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    /**
     * 设置一个对象
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean set(KeyPrefix prefix,String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            int seconds = prefix.expireSecondes();
            if (seconds <= 0) {
                jedis.set(prefix.getPrefix()+key,str);//UserKey:idkey1
            }else{
                jedis.setex(prefix.getPrefix() + key, seconds, str);
            }
            return true;
        }finally {
            jedis.close();
        }
    }

    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String)value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        }else{
            return JSON.toJSONString(value);
        }
    }

    /**
     * 判断对象是否存在
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix,String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(prefix.getPrefix()+key);
        }finally {
            jedis.close();
        }
    }    /**
     * 删除
     * @param prefix
     * @param key
     *
     * @return
     */
    public boolean delete(KeyPrefix prefix,String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long del = jedis.del(prefix.getPrefix() + key);
            return del > 0;
        }finally {
            jedis.close();
        }
    }

    /**
     * 增加一个key
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.decr(prefix.getPrefix()+key);
        }finally {
            jedis.close();
        }
    }

    /**
     * 减少一个key
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(prefix.getPrefix()+key);
        }finally {
            jedis.close();
        }
    }


}
