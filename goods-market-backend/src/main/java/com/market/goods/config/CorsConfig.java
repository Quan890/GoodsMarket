package com.market.goods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域 CORS 全局配置
 *
 * 说明：SpringBoot 4.x（Spring Framework 7.x）中 WebMvcConfigurer#addCorsMappings
 *       属于较旧写法，推荐使用 CorsFilter Bean 方式注册，兼容性更好、优先级更高。
 *
 * @author goods-market
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 允许携带 Cookie（设为 true 时 origin 不能为 "*"，需指定具体域名）
        config.setAllowCredentials(true);

        // 3. 允许的前端来源域名（开发环境可放开，生产环境应限制为具体域名）
        config.addAllowedOriginPattern("*");

        // 4. 允许的请求头
        config.addAllowedHeader("*");

        // 5. 允许的 HTTP 方法（GET / POST / PUT / DELETE / OPTIONS 等）
        config.addAllowedMethod("*");

        // 6. 暴露响应头（前端 JS 可读取的响应头）
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");

        // 7. 预检请求缓存时间（秒），减少 OPTIONS 请求次数
        config.setMaxAge(3600L);

        // 8. 注册到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
