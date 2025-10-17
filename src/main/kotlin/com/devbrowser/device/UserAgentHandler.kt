package com.devbrowser.device

import org.cef.callback.CefCallback
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse

/**
 * User-Agent 切换处理器
 * 用于拦截网络请求并修改 User-Agent 头部
 *
 * @property mobileDevice 移动设备配置（null 表示 PC 模式）
 */
class UserAgentHandler(
    private var mobileDevice: MobileDevice? = null
) : CefResourceRequestHandlerAdapter() {

    /**
     * 在资源加载前修改请求头
     */
    override fun onBeforeResourceLoad(
        browser: org.cef.browser.CefBrowser?,
        frame: org.cef.browser.CefFrame?,
        request: CefRequest?
    ): Boolean {
        if (request != null && mobileDevice != null) {
            // 设置移动端 User-Agent
            request.setHeaderByName("User-Agent", mobileDevice!!.userAgent, true)
        }
        return false
    }

    /**
     * 设置移动设备配置
     * @param device 移动设备配置（null 表示切换回 PC 模式）
     */
    fun setMobileDevice(device: MobileDevice?) {
        this.mobileDevice = device
    }

    /**
     * 获取当前移动设备配置
     */
    fun getMobileDevice(): MobileDevice? {
        return mobileDevice
    }

    /**
     * 检查是否处于移动端模式
     */
    fun isMobileMode(): Boolean {
        return mobileDevice != null
    }
}
