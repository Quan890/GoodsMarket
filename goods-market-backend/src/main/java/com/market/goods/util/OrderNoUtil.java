package com.market.goods.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 全局唯一订单号生成工具
 *
 * 订单号格式：GM + yyyyMMddHHmmss + 6位序列号 + 2位随机数
 * 示例：GM2026061415301200000158
 *       GM   20260614153012  000001    58
 *       前缀  时间戳(14位)    序列(6位)  随机(2位)
 *
 * 特性：
 *   - 单机每秒可生成约 100万 个不重复订单号
 *   - 序列号到达最大值后自动归零重新计数
 *   - 2位随机数增加散列性，防止并发冲突
 *
 * @author goods-market
 */
public class OrderNoUtil {

    private OrderNoUtil() {
        // 工具类禁止实例化
    }

    /** 订单号前缀 */
    private static final String PREFIX = "GM";

    /** 时间格式 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 序列号（AtomicLong 保证线程安全，最大 999999） */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /** 序列号最大值 */
    private static final long MAX_SEQUENCE = 999999L;

    /**
     * 生成全局唯一订单号
     *
     * @return 24位订单号字符串，如 "GM2026061415301200000158"
     */
    public static String generate() {
        // 1. 获取当前时间戳（14位：yyyyMMddHHmmss）
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // 2. 获取并递增序列号（到达最大值后归零）
        long seq = SEQUENCE.incrementAndGet();
        if (seq > MAX_SEQUENCE) {
            SEQUENCE.compareAndSet(seq, 0);
            seq = SEQUENCE.incrementAndGet();
        }

        // 3. 2位随机数（增加散列性，避免同一毫秒内的碰撞）
        int random = ThreadLocalRandom.current().nextInt(100);

        // 4. 拼接订单号：前缀 + 时间戳 + 序列号(6位补零) + 随机数(2位补零)
        return PREFIX + timestamp
                + String.format("%06d", seq)
                + String.format("%02d", random);
    }
}
