package com.market.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类 — 对应 merchant 表
 *
 * audit_status 字段含义：0=待审核 1=审核通过 2=驳回
 *
 * @author goods-market
 */
@Data
@TableName("merchant")
public class Merchant {

    /** 主键ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
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

    /** 审核备注 */
    private String auditRemark;

    /** 逻辑删除：0=未删除 1=已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
