package com.market.goods.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家信息视图对象
 *
 * 返回场景：
 *   1. 商家个人中心（查看自己的店铺信息）
 *   2. 管理员商家列表（审核管理）
 *   3. 商品详情页展示所属店铺信息
 *
 * @author goods-market
 */
@Data
public class MerchantVO {

    /** 商家ID */
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 店铺名称 */
    private String shopName;

    /** 店铺Logo */
    private String shopLogo;

    /** 店铺描述 */
    private String description;

    /** 营业执照号 */
    private String licenseNo;

    /** 营业执照图片 */
    private String licenseImg;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 经营地址 */
    private String address;

    /**
     * 审核状态
     * 0=待审核 1=审核通过 2=驳回
     */
    private Integer auditStatus;

    /**
     * 审核状态描述（前端直接展示）
     * "待审核" / "审核通过" / "已驳回"
     */
    private String auditStatusDesc;

    /** 审核备注（驳回原因等） */
    private String auditRemark;

    /** 入驻时间 */
    private LocalDateTime createTime;

    // ========== 统计信息（业务层按需填充） ==========

    /** 店铺商品总数 */
    private Integer productCount;

    /** 店铺累计订单数 */
    private Long orderCount;
}
