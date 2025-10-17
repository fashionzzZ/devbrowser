package com.devbrowser.theme

import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.Color
import javax.swing.SwingUtilities

/**
 * Darcula主题适配器
 * 负责将IDEA的Darcula主题应用到网页内容
 */
class DarculaThemeAdapter(
    private val browser: JBCefBrowser,
    initialEnabled: Boolean = false
) {

    // 主题颜色缓存
    private var backgroundColor: String = ""
    private var textColor: String = ""
    private var linkColor: String = ""

    // 主题启用状态
    private var isEnabled: Boolean = initialEnabled

    init {
        // 初始化主题颜色
        updateThemeColors()

        // 监听页面加载完成事件，自动注入主题
        setupLoadHandler()
    }

    /**
     * 更新主题颜色
     * 从IDEA当前主题获取颜色值
     */
    private fun updateThemeColors() {
        backgroundColor = JBColor.background().toHexString()
        textColor = JBColor.foreground().toHexString()
        linkColor = getLinkColor().toHexString()
    }

    /**
     * 获取链接颜色
     * 使用IDEA默认链接颜色
     */
    private fun getLinkColor(): Color {
        return Color(0x589DF6) // IDEA默认链接颜色
    }

    /**
     * 设置加载事件处理器
     * 在页面加载完成后自动应用主题（如果已启用）
     */
    private fun setupLoadHandler() {
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(
                browser: CefBrowser?,
                frame: CefFrame?,
                httpStatusCode: Int
            ) {
                // 只处理主框架，且主题已启用时才应用
                if (frame?.isMain == true && isEnabled) {
                    SwingUtilities.invokeLater {
                        applyTheme()
                    }
                }
            }
        }, browser.cefBrowser)
    }

    /**
     * 应用Darcula主题到当前页面
     * 通过JavaScript注入CSS样式
     */
    fun applyTheme() {
        // 更新颜色（以防主题已切换）
        updateThemeColors()

        // 构建CSS样式
        val css = buildThemeCSS()

        // 构建JavaScript注入代码
        val javascript = """
            (function() {
                // 移除之前注入的主题样式（如果存在）
                var oldStyle = document.getElementById('dev-browser-theme');
                if (oldStyle) {
                    oldStyle.remove();
                }

                // 创建新的样式元素
                var style = document.createElement('style');
                style.id = 'dev-browser-theme';
                style.textContent = `$css`;

                // 添加到文档头部
                document.head.appendChild(style);
            })();
        """.trimIndent()

        try {
            // 执行JavaScript
            browser.cefBrowser.executeJavaScript(
                javascript,
                browser.cefBrowser.url,
                0
            )
        } catch (e: Exception) {
            // JavaScript执行失败，静默处理
        }
    }

    /**
     * 构建主题CSS样式
     * @return CSS样式字符串
     */
    private fun buildThemeCSS(): String {
        return """
            /* DevBrowser - Darcula Theme */
            body {
                background-color: $backgroundColor !important;
                color: $textColor !important;
            }

            /* 链接颜色 */
            a {
                color: $linkColor !important;
            }

            /* 输入框和文本区域 */
            input, textarea, select {
                background-color: #3C3F41 !important;
                color: $textColor !important;
                border-color: #555555 !important;
            }

            /* 按钮 */
            button {
                background-color: #4C5052 !important;
                color: $textColor !important;
                border-color: #555555 !important;
            }

            /* 代码块 */
            code, pre {
                background-color: #2B2B2B !important;
                color: #A9B7C6 !important;
            }

            /* 表格 */
            table {
                background-color: $backgroundColor !important;
                color: $textColor !important;
            }

            th, td {
                border-color: #555555 !important;
            }

            /* 滚动条 (WebKit) */
            ::-webkit-scrollbar {
                width: 12px;
                height: 12px;
            }

            ::-webkit-scrollbar-track {
                background: #2B2B2B;
            }

            ::-webkit-scrollbar-thumb {
                background: #4C5052;
                border-radius: 6px;
            }

            ::-webkit-scrollbar-thumb:hover {
                background: #5C6062;
            }
        """.trimIndent()
    }

    /**
     * 将Color对象转换为十六进制字符串
     * @return #RRGGBB格式的颜色字符串
     */
    private fun Color.toHexString(): String {
        return String.format("#%02x%02x%02x", red, green, blue)
    }

    /**
     * 启用主题
     * 立即应用主题CSS到当前页面
     */
    fun enableTheme() {
        if (!isEnabled) {
            isEnabled = true
            applyTheme()
        }
    }

    /**
     * 禁用主题
     * 移除已注入的主题CSS
     */
    fun disableTheme() {
        if (isEnabled) {
            isEnabled = false
            removeTheme()
        }
    }

    /**
     * 移除已注入的主题样式
     */
    private fun removeTheme() {
        val javascript = """
            (function() {
                var style = document.getElementById('dev-browser-theme');
                if (style) {
                    style.remove();
                }
            })();
        """.trimIndent()

        try {
            browser.cefBrowser.executeJavaScript(
                javascript,
                browser.cefBrowser.url,
                0
            )
        } catch (e: Exception) {
            // JavaScript执行失败，静默处理
        }
    }

    /**
     * 获取当前主题启用状态
     */
    fun isThemeEnabled(): Boolean {
        return isEnabled
    }
}
