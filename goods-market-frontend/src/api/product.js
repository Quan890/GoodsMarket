import request from '@/utils/request'

/**
 * 商品模块 API
 */

/** 商品分页列表（公开） */
export function getProductList(params) {
  return request.get('/product/list', { params })
}

/** 商品详情（公开） */
export function getProductDetail(id) {
  return request.get(`/product/detail/${id}`)
}
