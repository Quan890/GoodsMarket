package com.market.goods.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.market.goods.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 阿里云短信工具类
 *
 * 功能：
 *   1. 生成 6 位随机验证码
 *   2. 调用阿里云短信 API 发送验证码（生产环境）
 *   3. 模拟模式：仅打印验证码到控制台（开发环境）
 *   4. 通过 RedisUtil 存储验证码（5分钟有效期）
 *   5. 60秒防频繁发送限制
 *
 * 使用流程：
 *   1. 前端调用 /user/send-code?phone=13800138000
 *   2. SmsUtil.sendVerificationCode("13800138000") 生成并发送验证码
 *   3. 用户输入验证码后，SmsUtil.verifyCode("13800138000", "123456") 校验
 *
 * @author goods-market
 */
@Slf4j
@Component
public class SmsUtil {

    private final RedisUtil redisUtil;
    private Client smsClient;
    private final String signName;
    private final String templateCode;
    private final boolean mockMode;

    /**
     * 构造注入 — 初始化阿里云短信客户端
     *
     * @param redisUtil         Redis 工具类
     * @param accessKeyId       阿里云 AccessKey ID
     * @param accessKeySecret   阿里云 AccessKey Secret
     * @param endpoint          短信 API 端点
     * @param signName          短信签名
     * @param templateCode      短信模板ID
     */
    public SmsUtil(
            RedisUtil redisUtil,
            @Value("${aliyun.sms.access-key-id:}") String accessKeyId,
            @Value("${aliyun.sms.access-key-secret:}") String accessKeySecret,
            @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}") String endpoint,
            @Value("${aliyun.sms.sign-name:}") String signName,
            @Value("${aliyun.sms.template-code:}") String templateCode) {
        this.redisUtil = redisUtil;
        this.signName = signName;
        this.templateCode = templateCode;

        // 检查是否为占位符配置（开发阶段）
        boolean isConfigReady = accessKeyId != null && !accessKeyId.isBlank() 
                && !accessKeyId.contains("YOUR_") 
                && accessKeySecret != null && !accessKeySecret.isBlank()
                && !accessKeySecret.contains("YOUR_");

        if (isConfigReady) {
            // 生产环境：初始化真实短信客户端
            try {
                Config config = new Config()
                        .setAccessKeyId(accessKeyId)
                        .setAccessKeySecret(accessKeySecret)
                        .setEndpoint(endpoint);
                this.smsClient = new Client(config);
                this.mockMode = false;
                log.info("阿里云短信服务已初始化（真实模式）");
            } catch (Exception e) {
                log.error("初始化阿里云短信客户端失败：{}", e.getMessage(), e);
                throw new BusinessException("短信服务初始化失败");
            }
        } else {
            // 开发环境：使用模拟模式
            this.smsClient = null;
            this.mockMode = true;
            log.warn("阿里云短信未配置或为占位符，已启用模拟模式（验证码将打印到控制台）");
        }
    }

    /**
     * 发送短信验证码
     *
     * 流程：
     *   1. 检查 60 秒内是否已发送（防频繁发送）
     *   2. 生成 6 位随机验证码
     *   3. 调用阿里云短信 API 发送（生产）或打印到控制台（开发）
     *   4. 将验证码存入 Redis（5分钟有效期）
     *   5. 设置 60 秒发送频率锁
     *
     * @param phone 手机号（如 13800138000）
     * @throws BusinessException 发送过于频繁或发送失败时抛出
     */
    public void sendVerificationCode(String phone) {
        // 1. 检查 60 秒防频繁发送限制
        if (redisUtil.isSmsSendLocked(phone)) {
            throw new BusinessException("发送过于频繁，请60秒后重试");
        }

        // 2. 生成 6 位随机验证码（100000 ~ 999999）
        String code = generateCode();

        // 3. 发送短信（区分真实模式和模拟模式）
        if (mockMode) {
            // 开发环境：打印验证码到控制台
            log.info("【模拟短信】验证码已生成 - 手机号: {}, 验证码: {}", phone, code);
            System.out.println("========================================");
            System.out.println("【模拟短信验证码】");
            System.out.println("手机号: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("========================================");
        } else {
            // 生产环境：调用阿里云短信 API
            try {
                SendSmsRequest request = new SendSmsRequest()
                        .setPhoneNumbers(phone)
                        .setSignName(signName)
                        .setTemplateCode(templateCode)
                        .setTemplateParam("{\"code\":\"" + code + "\"}");

                SendSmsResponse response = smsClient.sendSms(request);

                // 判断发送结果
                if (!"OK".equalsIgnoreCase(response.getBody().getCode())) {
                    log.error("短信发送失败：phone={}, code={}, message={}",
                            phone, response.getBody().getCode(), response.getBody().getMessage());
                    throw new BusinessException("短信发送失败，请稍后重试");
                }

                log.info("短信验证码已发送：phone={}", phone);

            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("短信发送异常：phone={}, error={}", phone, e.getMessage(), e);
                throw new BusinessException("短信发送失败，请稍后重试");
            }
        }

        // 4. 将验证码存入 Redis（5分钟有效期）
        redisUtil.saveSmsCode(phone, code);

        // 5. 设置 60 秒发送频率锁
        redisUtil.tryLockSmsSend(phone);
    }

    /**
     * 校验短信验证码
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=验证通过，false=验证码错误或已过期
     */
    public boolean verifyCode(String phone, String code) {
        return redisUtil.verifySmsCode(phone, code);
    }

    /**
     * 生成 6 位随机数字验证码
     *
     * 使用 ThreadLocalRandom 避免多线程竞争（JDK21 推荐方式）
     */
    private String generateCode() {
        int num = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(num);
    }
}
