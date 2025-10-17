package com.devbrowser.device

import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter

/**
 * 移动端视口适配器
 * 通过 JavaScript 注入来模拟移动设备的视口特性
 *
 * @property browser JCEF 浏览器实例
 * @property mobileDevice 移动设备配置（null 表示 PC 模式）
 */
class MobileViewportAdapter(
    private val browser: JBCefBrowser,
    private var mobileDevice: MobileDevice? = null
) {

    init {
        // 监听页面加载完成事件，自动注入视口脚本
        setupLoadHandler()
    }

    /**
     * 设置页面加载事件监听器
     */
    private fun setupLoadHandler() {
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(
                browser: CefBrowser?,
                frame: CefFrame?,
                httpStatusCode: Int
            ) {
                // 只处理主框架
                if (frame?.isMain == true && mobileDevice != null) {
                    applyMobileViewport()
                }
            }
        }, browser.cefBrowser)
    }

    /**
     * 应用移动端视口设置
     * 通过 JavaScript 注入来修改页面的视口属性
     */
    fun applyMobileViewport() {
        val device = mobileDevice ?: return

        val javascript = buildViewportScript(device)

        try {
            browser.cefBrowser.executeJavaScript(
                javascript,
                browser.cefBrowser.url,
                0
            )
        } catch (e: Exception) {
            // JavaScript 执行失败，静默处理
        }
    }

    /**
     * 移除移动端视口设置，恢复 PC 模式
     */
    fun removeMobileViewport() {
        val javascript = """
            (function() {
                // 移除注入的 viewport meta 标签
                var meta = document.querySelector('meta[name="mobile-viewport"]');
                if (meta) {
                    meta.remove();
                }

                // 注意：已覆盖的 window 属性无法完全恢复，需要刷新页面
                console.log('[DevBrowser] Switched to PC mode. Page refresh recommended.');
            })();
        """.trimIndent()

        try {
            browser.cefBrowser.executeJavaScript(
                javascript,
                browser.cefBrowser.url,
                0
            )
        } catch (e: Exception) {
            // JavaScript 执行失败，静默处理
        }
    }

    /**
     * 构建视口注入脚本
     */
    private fun buildViewportScript(device: MobileDevice): String {
        return """
            (function() {
                // 1. 注入 viewport meta 标签
                var existingMeta = document.querySelector('meta[name="mobile-viewport"]');
                if (existingMeta) {
                    existingMeta.remove();
                }

                var meta = document.createElement('meta');
                meta.name = 'mobile-viewport';
                meta.content = 'width=${device.width}, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                document.head.appendChild(meta);

                // 2. 覆盖 viewport 相关属性
                try {
                    Object.defineProperty(window, 'innerWidth', {
                        value: ${device.width},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window, 'innerHeight', {
                        value: ${device.height},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window, 'outerWidth', {
                        value: ${device.width},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window, 'outerHeight', {
                        value: ${device.height},
                        writable: false,
                        configurable: true
                    });

                    // 3. 设置设备像素比
                    Object.defineProperty(window, 'devicePixelRatio', {
                        value: ${device.devicePixelRatio},
                        writable: false,
                        configurable: true
                    });

                    // 4. 添加触摸能力标识
                    if (!('ontouchstart' in window)) {
                        Object.defineProperty(window, 'ontouchstart', {
                            value: null,
                            writable: true,
                            configurable: true
                        });
                    }

                    // 5. 修改 screen 对象属性
                    Object.defineProperty(window.screen, 'width', {
                        value: ${device.width},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window.screen, 'height', {
                        value: ${device.height},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window.screen, 'availWidth', {
                        value: ${device.width},
                        writable: false,
                        configurable: true
                    });

                    Object.defineProperty(window.screen, 'availHeight', {
                        value: ${device.height},
                        writable: false,
                        configurable: true
                    });

                    console.log('[DevBrowser] Mobile viewport applied: ${device.name}');
                } catch (e) {
                    console.error('[DevBrowser] Failed to override viewport properties:', e);
                }
            })();
        """.trimIndent()
    }

    /**
     * 设置移动设备配置
     * @param device 移动设备配置（null 表示切换回 PC 模式）
     */
    fun setMobileDevice(device: MobileDevice?) {
        this.mobileDevice = device

        if (device != null) {
            applyMobileViewport()
        } else {
            removeMobileViewport()
        }
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
