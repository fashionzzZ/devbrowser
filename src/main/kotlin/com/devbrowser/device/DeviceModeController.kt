package com.devbrowser.device

import com.intellij.ui.jcef.JBCefBrowser

/**
 * 设备模式控制器
 * 协调 User-Agent 和视口适配器，统一管理设备模式切换
 *
 * @property browser JCEF 浏览器实例
 */
class DeviceModeController(private val browser: JBCefBrowser) {

    // User-Agent 处理器
    private val userAgentHandler: UserAgentHandler = UserAgentHandler()

    // 视口适配器
    private val viewportAdapter: MobileViewportAdapter = MobileViewportAdapter(browser)

    // 当前移动设备配置
    private var currentDevice: MobileDevice? = null

    init {
        // 注册 User-Agent 处理器到浏览器客户端
        registerUserAgentHandler()
    }

    /**
     * 注册 User-Agent 处理器
     */
    private fun registerUserAgentHandler() {
        // 创建一个自定义的 RequestHandler 来使用我们的 UserAgentHandler
        val requestHandler = object : org.cef.handler.CefRequestHandlerAdapter() {
            override fun getResourceRequestHandler(
                browser: org.cef.browser.CefBrowser?,
                frame: org.cef.browser.CefFrame?,
                request: org.cef.network.CefRequest?,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String?,
                disableDefaultHandling: org.cef.misc.BoolRef?
            ): org.cef.handler.CefResourceRequestHandler? {
                return userAgentHandler
            }
        }

        browser.jbCefClient.addRequestHandler(requestHandler, browser.cefBrowser)
    }

    /**
     * 切换到移动端模式
     * @param device 移动设备配置（默认使用 iPhone 14）
     */
    fun switchToMobileMode(device: MobileDevice = MobileDevice.getDefault()) {
        currentDevice = device

        // 设置 User-Agent
        userAgentHandler.setMobileDevice(device)

        // 设置视口
        viewportAdapter.setMobileDevice(device)

        // 刷新页面以应用新的 User-Agent
        browser.cefBrowser.reload()
    }

    /**
     * 切换到 PC 模式
     */
    fun switchToPCMode() {
        currentDevice = null

        // 恢复默认 User-Agent
        userAgentHandler.setMobileDevice(null)

        // 移除移动端视口设置
        viewportAdapter.setMobileDevice(null)

        // 刷新页面以应用默认 User-Agent
        browser.cefBrowser.reload()
    }

    /**
     * 切换模式（在 PC 和移动端之间切换）
     */
    fun toggleMode() {
        if (isMobileMode()) {
            switchToPCMode()
        } else {
            switchToMobileMode()
        }
    }

    /**
     * 检查是否处于移动端模式
     */
    fun isMobileMode(): Boolean {
        return currentDevice != null
    }

    /**
     * 获取当前设备配置
     */
    fun getCurrentDevice(): MobileDevice? {
        return currentDevice
    }

    /**
     * 获取当前模式描述
     */
    fun getCurrentModeDescription(): String {
        return if (isMobileMode()) {
            currentDevice?.getDescription() ?: "移动端模式"
        } else {
            "PC 模式"
        }
    }
}
