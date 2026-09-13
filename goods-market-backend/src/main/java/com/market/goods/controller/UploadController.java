package com.market.goods.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.market.goods.exception.BusinessException;
import com.market.goods.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 通用图片上传 Controller
 *
 * 权限：需要登录（商家上传商品图、用户上传头像等）
 *
 * 保存位置：本地磁盘 uploads/images/yyyyMM/uuid.ext（application.yml app.upload-dir 可配置）
 * 访问地址：/api/images/upload/yyyyMM/uuid.ext（WebResourceConfig 静态资源映射）
 *
 * @author goods-market
 */
@Slf4j
@Tag(name = "通用上传", description = "图片上传（需登录）")
@RestController
@RequestMapping("/upload")
public class UploadController {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload-dir:uploads/images}")
    private String uploadDir;

    /**
     * 上传图片
     *
     * 请求：POST /api/upload/image
     * 参数：multipart/form-data，字段名 file
     *
     * 返回：{ "url": "/api/images/upload/202609/xxx.png" }
     */
    @Operation(summary = "上传图片", description = "上传jpg/png/gif/webp图片，返回可访问的URL")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }

        // 2. 扩展名白名单校验（同时校验内容类型，防伪装文件）
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("文件内容不是图片");
        }

        // 3. 按月分目录存储：uploads/images/yyyyMM/uuid.ext
        String monthDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path dirPath = Paths.get(uploadDir, monthDir).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);
            Path targetPath = dirPath.resolve(fileName);
            file.transferTo(targetPath.toFile());
            log.info("图片上传成功：user={}, path={}, size={}B",
                    StpUtil.getLoginIdAsLong(), targetPath, file.getSize());
        } catch (Exception e) {
            log.error("图片保存失败：{}", e.getMessage(), e);
            throw new BusinessException("图片上传失败，请稍后重试");
        }

        String url = "/api/images/upload/" + monthDir + "/" + fileName;
        return Result.ok(Map.of("url", url), "上传成功");
    }

    /**
     * 提取文件扩展名（小写），无扩展名返回空串
     */
    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
