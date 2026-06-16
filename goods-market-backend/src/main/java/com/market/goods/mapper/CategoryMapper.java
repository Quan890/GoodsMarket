package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.market.goods.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
