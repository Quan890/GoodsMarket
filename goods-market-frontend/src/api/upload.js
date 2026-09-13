import request from '@/utils/request'

/**
 * 通用上传接口
 *
 * 对接后端 UploadController（/upload，需登录）
 */

/**
 * 上传图片
 *
 * @param {File} file - 图片文件（jpg/jpeg/png/gif/webp，≤10MB）
 * @returns {Promise<Result<{url: string}>>} 返回可访问的图片 URL
 *
 * @example
 *   const res = await uploadImage(file)
 *   console.log(res.data.url) // /api/images/upload/202609/xxx.png
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
