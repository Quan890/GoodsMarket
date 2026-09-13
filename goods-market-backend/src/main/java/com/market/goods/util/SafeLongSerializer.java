package com.market.goods.util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Long 类型安全序列化器
 *
 * 仅当数值超出 JavaScript 安全整数范围（±(2^53-1)）时才输出为字符串，
 * 例如雪花算法 ID（19位）；普通小数值（如分页 total、库存）保持数字输出。
 *
 * 与前端 json-bigint（storeAsString: true）的解析行为保持一致：
 * 大数 → 字符串，小数 → 数字，前后端双保险防精度丢失。
 *
 * @author goods-market
 */
public class SafeLongSerializer extends ValueSerializer<Long> {

    /** JS 安全整数上限：Number.MAX_SAFE_INTEGER = 2^53 - 1 */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    /** JS 安全整数下限：Number.MIN_SAFE_INTEGER = -(2^53 - 1) */
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    public static final SafeLongSerializer INSTANCE = new SafeLongSerializer();

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        long v = value;
        if (v >= MIN_SAFE_INTEGER && v <= MAX_SAFE_INTEGER) {
            gen.writeNumber(v);
        } else {
            gen.writeString(String.valueOf(v));
        }
    }
}
