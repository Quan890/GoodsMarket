package com.market.goods.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员审核商家入参
 *
 * 接口：PUT /admin/merchant/audit
 * 说明：管理员对商家入驻申请进行审核（通过/驳回），驳回时必须填写备注说明原因
 *
 * @author goods-market
 */
@Data
public class AuditMerchantDTO {

    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    /**
     * 审核结果：1=审核通过 2=驳回
     */
    @NotNull(message = "审核结果不能为空")
    @Min(value = 1, message = "审核结果不合法，1=通过 2=驳回")
    @Max(value = 2, message = "审核结果不合法，1=通过 2=驳回")
    private Integer auditStatus;

    /**
     * 审核备注（驳回时必填，说明驳回原因）
     */
    @Size(max = 500, message = "审核备注不能超过500个字符")
    private String auditRemark;
}
