package com.market.goods.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson 全局序列化配置
 *
 * 解决雪花算法生成的 Long 型 ID 超出 JavaScript 安全整数范围（2^53-1）的问题。
 * 将 Long/long 类型序列化为字符串，前端收到的是 "2066885747373412354" 而非 2066885747373412400。
 *
 * @author goods-market
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        mapper.registerModule(module);
        // 继承 application.yml 中 jackson 的配置
        mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
        return mapper;
    }
}
