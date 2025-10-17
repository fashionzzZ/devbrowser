package com.devbrowser.device

/**
 * 移动设备配置数据类
 * 定义移动设备的规格参数，用于模拟移动端浏览环境
 *
 * @property name 设备名称
 * @property width 视口宽度（像素）
 * @property height 视口高度（像素）
 * @property devicePixelRatio 设备像素比（DPR）
 * @property userAgent User-Agent 字符串
 */
data class MobileDevice(
    val name: String,
    val width: Int,
    val height: Int,
    val devicePixelRatio: Double,
    val userAgent: String
) {
    companion object {
        /**
         * iPhone 14 设备配置
         * - 屏幕尺寸: 390 x 844
         * - 设备像素比: 3.0
         * - iOS 17 User-Agent
         */
        val IPHONE_14 = MobileDevice(
            name = "iPhone 14",
            width = 390,
            height = 844,
            devicePixelRatio = 3.0,
            userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/17.0 Mobile/15E148 Safari/604.1"
        )

        /**
         * 获取默认移动设备（iPhone 14）
         */
        fun getDefault(): MobileDevice = IPHONE_14
    }

    /**
     * 验证设备配置是否有效
     */
    fun isValid(): Boolean {
        return width > 0 && height > 0 && devicePixelRatio > 0 && userAgent.isNotBlank()
    }

    /**
     * 获取设备描述信息
     */
    fun getDescription(): String {
        return "$name (${width}x${height}, DPR: $devicePixelRatio)"
    }
}
