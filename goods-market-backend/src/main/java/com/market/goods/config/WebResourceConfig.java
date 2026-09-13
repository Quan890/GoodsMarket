package com.market.goods.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 静态资源映射配置
 *
 * 上传的图片保存在本地磁盘（默认 项目根目录/uploads/images），
 * 通过 /images/upload/** 映射为静态资源对外访问。
 *
 * 完整访问路径：http://localhost:8080/api/images/upload/202609/xxx.png
 * 数据库中保存相对路径：/api/images/upload/202609/xxx.png
 *
 * @author goods-market
 */
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();

        registry.addResourceHandler("/images/upload/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
