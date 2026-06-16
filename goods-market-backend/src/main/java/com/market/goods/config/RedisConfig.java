package com.market.goods.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 序列化配置
 *
 * 背景：
 *   - 默认的 JdkSerializationRedisSerializer 在 JDK17+ 环境下
 *     因模块化限制（JPMS）会报 InaccessibleObjectException
 *   - 本配置使用 Jackson JSON 序列化，彻底规避 JDK21 模块访问问题
 *
 * 序列化策略：
 *   - Key：StringRedisSerializer（可读、节省空间）
 *   - Value：GenericJackson2JsonRedisSerializer（JSON 格式，含类型信息，反序列化无歧义）
 *
 * @author goods-market
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // ==================== Key 序列化器 ====================
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // ==================== Value 序列化器 ====================
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 初始化属性设置
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建 Jackson JSON 序列化器
     * 包含类型信息，反序列化时能正确还原为原始类型（如 UserVO、Product 等）
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置所有属性可见（包括 private）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用类型信息写入（@class 字段），反序列化时自动识别目标类型
        // 注意：SpringBoot 4 + Jackson 2.x 使用 LaissezFaireSubTypeValidator
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 注册 Java 8+ 时间模块（LocalDateTime / LocalDate 等）
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用日期时间戳格式（使用 ISO-8601 字符串格式）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
