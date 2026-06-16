package com.market.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.market.goods.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 商家表 Mapper 接口
 *
 * @author goods-market
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 管理员多条件分页查询商家
     *
     * @param page   分页参数
     * @param params 查询条件：shopName（店铺名模糊）、auditStatus（审核状态）
     * @return 分页结果
     */
    IPage<Merchant> selectMerchantPageByAdmin(Page<Merchant> page, @Param("params") Map<String, Object> params);
}
