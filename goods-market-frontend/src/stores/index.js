/**
 * Pinia 仓库统一导出入口
 *
 * 各仓库定义在独立文件中，此处集中 re-export，
 * 调用方统一从 '@/stores' 引入即可：
 *
 *   import { useUserStore, useCartStore } from '@/stores'
 */

export { useUserStore } from './user'
export { useCartStore } from './cart'
