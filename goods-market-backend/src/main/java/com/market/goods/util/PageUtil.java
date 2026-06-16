package com.market.goods.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页工具类
 *
 * 功能：
 * 1. 构建 MyBatis-Plus 分页查询参数 Page 对象
 * 2. 将 MyBatis-Plus 的 IPage 结果转换为前端友好的分页响应
 * 3. 提供分页结果封装类 PageResult
 *
 * 使用示例：
 *   // 构建分页参数
 *   Page&lt;Product&gt; page = PageUtil.buildPage(queryDTO);
 *
 *   // 执行查询（MyBatis-Plus Mapper 内置分页）
 *   IPage&lt;Product&gt; result = productMapper.selectPage(page, wrapper);
 *
 *   // 转换为前端响应
 *   PageResult&lt;ProductVO&gt; pageResult = PageUtil.toPageResult(result, ProductVO::fromEntity);
 *
 * @author goods-market
 */
public class PageUtil {

    /** 默认页码 */
    private static final int DEFAULT_PAGE = 1;

    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 10;

    /** 最大每页条数（防止前端传入过大值） */
    private static final int MAX_SIZE = 500;

    private PageUtil() {
        // 工具类禁止实例化
    }

    // ==================== 构建分页参数 ====================

    /**
     * 根据页码和每页条数构建 MyBatis-Plus Page 对象
     *
     * @param pageNum  页码（从1开始，小于1时自动修正为1）
     * @param pageSize 每页条数（范围 1~500，超出自动修正）
     * @return Page 分页参数对象
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize) {
        // 修正页码
        int page = Math.max(pageNum, DEFAULT_PAGE);
        // 修正每页条数：最小1，最大500
        int size = Math.max(1, Math.min(pageSize, MAX_SIZE));
        return new Page<>(page, size);
    }

    /**
     * 使用默认分页参数构建 Page 对象（第1页，每页10条）
     */
    public static <T> Page<T> buildDefaultPage() {
        return new Page<>(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    // ==================== 转换分页结果 ====================

    /**
     * 将 MyBatis-Plus IPage 结果转换为 PageResult（无类型转换）
     *
     * @param page MyBatis-Plus 分页查询结果
     * @return PageResult 分页响应对象
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    /**
     * 将 MyBatis-Plus IPage 结果转换为 PageResult（含 VO 转换）
     *
     * @param page     MyBatis-Plus 分页查询结果
     * @param converter 实体 → VO 的转换函数
     * @return PageResult 分页响应对象（records 中为 VO 类型）
     */
    public static <T, R> PageResult<R> toPageResult(IPage<T> page, Function<T, R> converter) {
        List<R> records = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream().map(converter).collect(Collectors.toList());

        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    // ==================== 分页结果封装类 ====================

    /**
     * 分页响应封装 — 直接返回给前端
     *
     * 响应格式：
     * {
     *   "records": [...],     // 当前页数据列表
     *   "total": 100,         // 总记录数
     *   "current": 1,         // 当前页码
     *   "size": 10,           // 每页条数
     *   "pages": 10           // 总页数
     * }
     *
     * @param <T> 数据类型（通常是 VO）
     */
    @Data
    public static class PageResult<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 当前页数据列表 */
        private List<T> records;

        /** 总记录数 */
        private long total;

        /** 当前页码 */
        private long current;

        /** 每页条数 */
        private long size;

        /** 总页数 */
        private long pages;

        public PageResult(List<T> records, long total, long current, long size, long pages) {
            this.records = records;
            this.total = total;
            this.current = current;
            this.size = size;
            this.pages = pages;
        }
    }
}
