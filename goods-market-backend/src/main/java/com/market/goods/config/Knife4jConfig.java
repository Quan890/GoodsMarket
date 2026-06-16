package com.market.goods.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j（Swagger增强）接口文档配置
 *
 * 访问地址：http://localhost:8080/doc.html
 *
 * @author goods-market
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("好物集市 API 接口文档")
                        .description("好物集市电商系统后端接口 — 基于 JDK21 + SpringBoot4.1.0")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("goods-market")
                                .email("admin@goodsmarket.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
