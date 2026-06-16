package com.market.goods.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 封装验证码存取、过期操作等常用方法。
 * 底层使用 StringRedisTemplate（Key/Value 均为 String 类型）。
 *
 * Redis Key 命名规范：
 *   sms:code:phone:{手机号}        → 短信验证码
 *   sms:lock:phone:{手机号}        → 短信发送频率锁（60秒防刷）
 *
 * @author goods-market
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    // ==================== 短信验证码相关 Key 前缀 ====================

    /** 短信验证码前缀：sms:code:phone:{phone} */
    private static final String SMS_CODE_PREFIX = "sms:code:phone:";

    /** 短信发送频率锁前缀：sms:lock:phone:{phone} */
    private static final String SMS_LOCK_PREFIX = "sms:lock:phone:";

    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 发送频率限制（秒） */
    private static final int SEND_INTERVAL_SECONDS = 60;

    // ==================== 验证码存取 ====================

    /**
     * 存储短信验证码到 Redis
     *
     * @param phone 手机号
     * @param code  验证码（6位数字）
     */
    public void saveSmsCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 获取短信验证码
     *
     * @param phone 手机号
     * @return 验证码，不存在或已过期返回 null
     */
    public String getSmsCode(String phone) {
        String key = SMS_CODE_PREFIX + phone;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除短信验证码（验证成功后调用，防止重复使用）
     *
     * @param phone 手机号
     */
    public void deleteSmsCode(String phone) {
        String key = SMS_CODE_PREFIX + phone;
        stringRedisTemplate.delete(key);
    }

    /**
     * 校验短信验证码
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=验证通过，false=验证码错误或已过期
     */
    public boolean verifySmsCode(String phone, String code) {
        String cachedCode = getSmsCode(phone);
        if (cachedCode == null) {
            return false;
        }
        boolean matched = cachedCode.equals(code);
        if (matched) {
            // 验证成功后立即删除，防止重复使用
            deleteSmsCode(phone);
        }
        return matched;
    }

    // ==================== 发送频率限制 ====================

    /**
     * 设置短信发送频率锁（60秒内不可重复发送）
     *
     * @param phone 手机号
     * @return true=设置成功（可以发送），false=已存在锁（发送过于频繁）
     */
    public boolean tryLockSmsSend(String phone) {
        String key = SMS_LOCK_PREFIX + phone;
        // setIfAbsent 相当于 SETNX，key 不存在时才设置成功
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 检查是否在发送频率限制内
     *
     * @param phone 手机号
     * return true=在限制内（不可发送），false=不在限制内（可以发送）
     */
    public boolean isSmsSendLocked(String phone) {
        String key = SMS_LOCK_PREFIX + phone;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    // ==================== 通用方法 ====================

    /**
     * 设置 String 类型键值对（带过期时间）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取 String 类型值
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定 key
     *
     * @param key 键
     * @return true=删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true=存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
