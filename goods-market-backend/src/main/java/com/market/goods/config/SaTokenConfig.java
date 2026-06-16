package com.market.goods.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 全局拦截配置
 *
 * 预设四角色拦截规则：
 *   role=3 管理员 —— /admin/** 路径
 *   role=2 商家   —— /merchant/** 路径
 *   role=1 普通用户 —— /user/** 路径
 *   role=0 游客   —— 仅公开接口，无需登录
 *
 * 注意：SpringBoot 4.x 中 addPathPatterns / excludePathPatterns
 *       仍属于 WebMvcConfigurer 标准 API，未被废弃。
 *
 * @author goods-market
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则写在 lambda 中
        registry.addInterceptor(new SaInterceptor(handle -> {

            // ==================== 管理员接口 ====================
            // /admin/** 所有路径必须登录且角色为 管理员(3)
            SaRouter.match("/admin/**")
                    .notMatch("/admin/login")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRole("admin");
                    });

            // ==================== 商家接口 ====================
            // /merchant/** 所有路径必须登录且角色为 商家(2) 或管理员(3)
            // apply 和 my/status 允许任何已登录用户访问（申请入驻 + 查询自己的申请状态）
            SaRouter.match("/merchant/**")
                    .notMatch("/merchant/login", "/merchant/register",
                              "/merchant/apply", "/merchant/my/status")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRoleOr("merchant", "admin");
                    });

            // ==================== 用户接口 ====================
            // /user/** 所有路径必须登录且角色为 用户(1) 或更高
            SaRouter.match("/user/**")
                    .notMatch("/user/login", "/user/login-password", "/user/register",
                            "/user/send-code", "/user/reset-password", "/user/captcha")
                    .check(r -> {
                        StpUtil.checkLogin();
                        StpUtil.checkRoleOr("user", "merchant", "admin");
                    });

            // ==================== 公开接口（无需登录） ====================
            // /public/**、/pay/wx/notify 等回调地址不做拦截
            // （默认不匹配的路径不拦截，如需强制登录可在此补充）

        })).addPathPatterns("/**")
           // 排除静态资源、接口文档、错误页面
           .excludePathPatterns(
                   "/error",
                   "/swagger-resources/**",
                   "/webjars/**",
                   "/v3/api-docs/**",
                   "/doc.html",
                   "/favicon.ico",
                   "/images/**"
           );
    }
}
