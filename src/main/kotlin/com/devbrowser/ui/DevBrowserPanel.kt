package com.devbrowser.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import com.devbrowser.settings.DevBrowserSettingsState
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * 主浏览器面板
 * 使用BorderLayout布局管理器：
 * - NORTH: 浏览器工具栏
 * - CENTER: JCEF浏览器
 */
class DevBrowserPanel(
    private val project: Project,
    private val toolWindow: ToolWindow
) : JPanel(BorderLayout()), Disposable {

    // JCEF浏览器实例（可能为null，如果JCEF不支持）
    private var browser: JBCefBrowser? = null

    // 浏览器工具栏
    private var browserToolbar: BrowserToolbar? = null

    // 设置状态管理
    private val settingsState = DevBrowserSettingsState.getInstance(project)

    init {
        // 设置面板边距
        border = JBUI.Borders.empty(8)

        // 检查JCEF支持
        if (JBCefApp.isSupported()) {
            initBrowser()
        } else {
            showUnsupportedMessage()
        }
    }

    /**
     * 初始化JCEF浏览器
     */
    private fun initBrowser() {
        try {
            // 创建JBCefBrowser实例
            browser = JBCefBrowser()

            // 创建浏览器工具栏
            browserToolbar = BrowserToolbar(browser!!, project)
            add(browserToolbar, BorderLayout.NORTH)

            // 加载上次保存的URL或默认页面
            val savedUrl = settingsState.state.lastUrl
            browser?.loadURL(savedUrl)

            // 将浏览器组件添加到面板中心
            add(browser!!.component, BorderLayout.CENTER)

        } catch (e: Exception) {
            // 如果创建失败，显示错误信息
            showErrorMessage("JCEF浏览器初始化失败: ${e.message}")
        }
    }

    /**
     * 显示不支持JCEF的提示
     */
    private fun showUnsupportedMessage() {
        val message = JLabel(
            "<html><center>" +
                    "<h2>JCEF不支持</h2>" +
                    "<p>您的IDEA版本或JDK不支持JCEF（Java Chromium Embedded Framework）。</p>" +
                    "<p>请确保：</p>" +
                    "<ul>" +
                    "<li>使用IntelliJ IDEA 2020.2或更高版本</li>" +
                    "<li>使用JetBrains Runtime (JBR) JDK</li>" +
                    "</ul>" +
                    "</center></html>",
            SwingConstants.CENTER
        )
        message.border = JBUI.Borders.empty(20)
        add(message, BorderLayout.CENTER)
    }

    /**
     * 显示错误信息
     */
    private fun showErrorMessage(errorMsg: String) {
        val message = JLabel(
            "<html><center>" +
                    "<h2>错误</h2>" +
                    "<p>$errorMsg</p>" +
                    "</center></html>",
            SwingConstants.CENTER
        )
        message.border = JBUI.Borders.empty(20)
        add(message, BorderLayout.CENTER)
    }

    /**
     * 资源清理
     * 当ToolWindow关闭时调用
     */
    override fun dispose() {
        // 保存当前URL
        browser?.let { br ->
            val currentUrl = br.cefBrowser.url
            if (!currentUrl.isNullOrEmpty()) {
                settingsState.state.lastUrl = currentUrl
            }
        }

        // 释放浏览器资源
        browser?.dispose()
        browser = null
        browserToolbar = null
        removeAll()
    }
}
