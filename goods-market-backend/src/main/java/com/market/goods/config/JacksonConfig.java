package com.market.goods.config;

import com.market.goods.util.SafeLongSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 3 序列化配置（Spring Boot 4.x 的 MVC JSON 转换使用 tools.jackson 3.x）
 *
 * 1. Long → 超出 JS 安全整数范围时输出字符串（雪花ID），小数值保持数字（分页 total 等）
 * 2. LocalDateTime → "yyyy-MM-dd HH:mm:ss"：
 *    Jackson 3 默认输出带 T 的 ISO 格式（"2026-09-13T10:56:41"），
 *    且 spring.jackson.date-format 只对 java.util.Date 生效，需单独注册序列化器
 *
 * 注意：Boot 4 会自动收集所有 JacksonModule 类型的 Bean 并注册到全局 JsonMapper，
 *      不要再声明 Jackson 2（com.fasterxml）的 ObjectMapper —— 它不会被 MVC使用
 *
 * @author goods-market
 */
@Configuration
public class JacksonConfig {

    /** 全局日期时间格式 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public SimpleModule goodsMarketJacksonModule() {
        SimpleModule module = new SimpleModule("goodsMarketModule");
        // Long → 超出 JS 安全整数才转字符串（防雪花ID精度丢失，不影响 total 等小数值）
        module.addSerializer(Long.class, SafeLongSerializer.INSTANCE);
        module.addSerializer(Long.TYPE, SafeLongSerializer.INSTANCE);
        // LocalDateTime → "yyyy-MM-dd HH:mm:ss"
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        return module;
    }
}

