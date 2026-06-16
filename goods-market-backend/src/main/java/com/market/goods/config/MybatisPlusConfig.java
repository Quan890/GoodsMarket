package com.market.goods.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 插件配置
 *
 * 包含：
 * 1. 分页插件 — 自动拼接 LIMIT 语句
 * 2. 乐观锁插件 — 自动在 UPDATE 语句中添加 version 条件
 *
 * 用法：在实体类中添加 @Version 注解的字段即可生效
 *       例：@Version private Integer version;
 *
 * @author goods-market
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 插件拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ========== 分页插件（必须在乐观锁之前添加） ==========
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大限制（防止前端传入过大 page size）
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // ========== 乐观锁插件 ==========
        // 执行逻辑：UPDATE product SET stock = stock - 1, version = version + 1
        //           WHERE id = ? AND version = ?
        // 如果 version 不匹配，更新影响行数为 0，业务层捕获后提示"操作失败，请重试"
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 配置 SqlSessionFactory（确保 MyBatis-Plus 正常工作）
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, 
                                                MybatisPlusInterceptor mybatisPlusInterceptor,
                                                MetaObjectHandler metaObjectHandler) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        // 关键：注入自动填充处理器
        globalConfig.setMetaObjectHandler(metaObjectHandler);
        sessionFactory.setGlobalConfig(globalConfig);
        
        // 设置插件拦截器
        sessionFactory.setPlugins(mybatisPlusInterceptor);
        
        // 设置 Mapper XML 文件位置（关键配置）
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:/mapper/**/*.xml"));
        
        return sessionFactory.getObject();
    }

    /**
     * 配置 SqlSessionTemplate
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
