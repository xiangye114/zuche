import { ElMessage } from 'element-plus'

/**
 * 下载文件
 * @param {Blob} blob - 文件blob
 * @param {string} filename - 文件名
 */
export function downloadFile(blob, filename) {
  const link = document.createElement('a')
  const url = window.URL.createObjectURL(blob)
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 导出Excel
 * @param {Promise} exportPromise - 导出请求的Promise
 * @param {string} filename - 文件名
 */
export async function exportExcel(exportPromise, filename = '导出数据.xlsx') {
  try {
    const response = await exportPromise
    
    // 检查是否是错误响应
    if (response.type === 'application/json') {
      const reader = new FileReader()
      reader.onload = () => {
        try {
          const error = JSON.parse(reader.result)
          ElMessage.error(error.message || '导出失败')
        } catch {
          ElMessage.error('导出失败')
        }
      }
      reader.readAsText(response)
      return
    }
    
    downloadFile(response, filename)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

export default {
  downloadFile,
  exportExcel
}
