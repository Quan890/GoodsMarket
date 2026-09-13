package com.market.goods.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.goods.dto.*;
import com.market.goods.entity.Merchant;
import com.market.goods.entity.Product;
import com.market.goods.entity.User;
import com.market.goods.enums.UserRoleEnum;
import com.market.goods.exception.BusinessException;
import com.market.goods.mapper.MerchantMapper;
import com.market.goods.mapper.ProductMapper;
import com.market.goods.mapper.UserMapper;
import com.market.goods.service.UserService;
import com.market.goods.util.CaptchaUtil;
import com.market.goods.util.RedisUtil;
import com.market.goods.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块 Service 实现类
 *
 * @author goods-market
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SmsUtil smsUtil;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 发送短信验证码 ====================
    @Override
    public void sendVerificationCode(SendCodeDTO dto) {
        smsUtil.sendVerificationCode(dto.getPhone());
        log.info("验证码发送成功：phone={}", dto.getPhone());
    }

    // ==================== 手机号 + 短信验证码登录 ====================
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        if (!smsUtil.verifyCode(dto.getPhone(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())
        );

        if (user == null) {
            // 首次登录自动注册（默认普通用户，否则无法使用购物车/下单等功能）
            user = new User();
            user.setPhone(dto.getPhone());
            user.setUsername("user_" + dto.getPhone());
            user.setNickname("用户" + dto.getPhone().substring(7));
            user.setRole(UserRoleEnum.USER.getCode());
            user.setStatus(1);
            user.setPassword(passwordEncoder.encode("SMS_LOGIN_" + System.currentTimeMillis()));
            userMapper.insert(user);
            log.info("新用户自动注册：phone={}, userId={}", dto.getPhone(), user.getId());
        } else {
            if (user.getStatus() == 0) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }
        }

        return doLogin(user);
    }

    // ==================== 用户名 + 密码登录 ====================
    @Override
    public Map<String, Object> loginByPassword(PasswordLoginDTO dto) {
        // 1. 校验图形验证码
        verifyCaptcha(dto.getCaptchaToken(), dto.getCaptchaCode());

        // 2. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        return doLogin(user);
    }

    // ==================== 用户注册 ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 校验短信验证码
        if (!smsUtil.verifyCode(dto.getPhone(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 校验用户名是否已存在
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (usernameCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 校验手机号是否已注册
        Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())
        );
        if (phoneCount > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getUsername());
        user.setRole(UserRoleEnum.USER.getCode());   // 注册默认为普通用户
        user.setStatus(1);
        userMapper.insert(user);

        log.info("用户注册成功：username={}, phone={}, userId={}", dto.getUsername(), dto.getPhone(), user.getId());
    }

    // ==================== 找回密码（重置密码） ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        // 1. 校验短信验证码
        if (!smsUtil.verifyCode(dto.getPhone(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())
        );
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);

        log.info("密码重置成功：phone={}", dto.getPhone());
    }

    // ==================== 修改密码（需登录） ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验原密码（短信自动注册的用户无密码，原密码不匹配时提示走找回密码）
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);

        // 修改密码后强制重新登录
        StpUtil.logout();

        log.info("用户修改密码成功：userId={}", userId);
    }

    // ==================== 注销账户 ====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId, DeleteAccountDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 管理员不能注销自己
        if (user.getRole() == UserRoleEnum.ADMIN.getCode()) {
            throw new BusinessException("管理员账号不允许注销");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 商家注销需要先删除所有商品
        if (user.getRole() == UserRoleEnum.MERCHANT.getCode()) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId)
            );
            if (merchant != null) {
                Long productCount = productMapper.selectCount(
                        new LambdaQueryWrapper<Product>()
                                .eq(Product::getMerchantId, merchant.getId())
                                .eq(Product::getDeleted, 0)
                );
                if (productCount > 0) {
                    throw new BusinessException("请先下架并删除所有商品后再注销账户（当前还有 " + productCount + " 件商品）");
                }
            }
        }

        // 逻辑删除用户
        userMapper.deleteById(userId);

        // 注销 Sa-Token 会话
        StpUtil.logout();

        log.info("用户注销成功：userId={}, username={}", userId, user.getUsername());
    }

    // ==================== 获取用户信息 ====================
    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("role", user.getRole());

        String roleName = switch (user.getRole()) {
            case 3 -> "admin";
            case 2 -> "merchant";
            case 1 -> "user";
            default -> "guest";
        };
        result.put("roleName", roleName);
        result.put("avatar", user.getAvatar());
        return result;
    }

    // ==================== 退出登录 ====================
    @Override
    public void logout() {
        long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        log.info("用户退出登录：userId={}", userId);
    }

    // ==================== 私有方法 ====================

    /**
     * Sa-Token 登录 + 返回 token 信息
     */
    private Map<String, Object> doLogin(User user) {
        StpUtil.login(user.getId());

        String roleName = switch (user.getRole()) {
            case 3 -> "admin";
            case 2 -> "merchant";
            case 1 -> "user";
            default -> "guest";
        };
        StpUtil.getSession().set("role", roleName);

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("role", user.getRole());
        result.put("roleName", roleName);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        return result;
    }

    /**
     * 校验图形验证码
     */
    private void verifyCaptcha(String captchaToken, String captchaCode) {
        String redisKey = CaptchaUtil.CAPTCHA_PREFIX + captchaToken;
        String cachedCode = redisUtil.get(redisKey);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请刷新验证码");
        }
        // 验证成功后删除，防止重复使用
        redisUtil.delete(redisKey);
        if (!cachedCode.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException("验证码错误");
        }
    }
}
