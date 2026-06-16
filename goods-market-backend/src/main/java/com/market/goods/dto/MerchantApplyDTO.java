package com.market.goods.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家入驻申请入参
 *
 * 接口：POST /merchant/apply
 * 说明：普通用户提交商家入驻申请，填写店铺信息和营业执照，提交后等待管理员审核
 *
 * @author goods-market
 */
@Data
public class MerchantApplyDTO {

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称不能超过100个字符")
    private String shopName;

    /**
     * 店铺Logo URL
     */
    private String shopLogo;

    /**
     * 店铺描述
     */
    @Size(max = 1000, message = "店铺描述不能超过1000个字符")
    private String description;

    /**
     * 营业执照号
     */
    private String licenseNo;

    /**
     * 营业执照图片 URL
     */
    private String licenseImg;

    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名不能超过50个字符")
    private String contactName;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /**
     * 经营地址
     */
    @NotBlank(message = "经营地址不能为空")
    @Size(max = 300, message = "经营地址不能超过300个字符")
    private String address;
}
