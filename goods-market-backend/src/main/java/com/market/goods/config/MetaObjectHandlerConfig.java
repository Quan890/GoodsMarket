package com.market.goods.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 配合实体类中的 @TableField(fill = FieldFill.INSERT) 注解使用：
 *   - insert 操作自动填充 createTime
 *   - update 操作自动填充 updateTime
 *
 * 原理：MyBatis-Plus 在执行 insert/update 前会回调本类的 insertFill/updateFill 方法，
 *       将指定字段的值注入到 SQL 中，无需业务代码手动 set。
 *
 * @author goods-market
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    /**
     * 插入操作自动填充
     *
     * @param metaObject 元对象（包含实体类的字段信息）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充 createTime 和 updateTime（插入时两者相同）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    /**
     * 更新操作自动填充
     *
     * @param metaObject 元对象（包含实体类的字段信息）
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充 updateTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
}
