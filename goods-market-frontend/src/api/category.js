import request from '@/utils/request'

/**
 * 分类模块 API
 */

/** 获取分类树（一级+二级） */
export function getCategoryTree() {
  return request.get('/category/tree')
}
