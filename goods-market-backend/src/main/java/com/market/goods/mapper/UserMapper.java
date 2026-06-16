package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用户表 Mapper 接口
 *
 * 继承 BaseMapper 获得单表 CRUD 能力，自定义方法在 XML 中实现
 *
 * @author goods-market
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 管理员多条件分页查询用户
     *
     * @param page   分页参数
     * @param params 查询条件：phone（手机号模糊）、role（角色）、status（状态）
     * @return 分页结果
     */
    IPage<User> selectUserPageByAdmin(Page<User> page, @Param("params") Map<String, Object> params);
}
