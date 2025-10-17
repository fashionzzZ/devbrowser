package com.devbrowser.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import com.devbrowser.bookmark.BookmarkActionButton
import com.devbrowser.bookmark.BookmarkListButton
import com.devbrowser.device.DeviceModeButton
import com.devbrowser.device.DeviceModeController
import com.devbrowser.settings.DevBrowserSettingsState
import com.devbrowser.theme.DarculaThemeAdapter
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * 浏览器控制工具栏
 * 提供地址栏、前进/后退/刷新按钮、主题开关、书签功能、设备模式切换
 */
class BrowserToolbar(
    private val browser: JBCefBrowser,
    private val project: Project
) : JPanel(BorderLayout()) {

    // 前进/后退/刷新按钮
    private val backButton: JButton = JButton(AllIcons.Actions.Back)
    private val forwardButton: JButton = JButton(AllIcons.Actions.Forward)
    private val refreshButton: JButton = JButton(AllIcons.Actions.Refresh)

    // 地址栏
    private val urlField: JBTextField = JBTextField()

    // 书签按钮
    private val bookmarkActionButton: BookmarkActionButton = BookmarkActionButton(project, browser)
    private val bookmarkListButton: BookmarkListButton = BookmarkListButton(project, browser)

    // 设备模式切换
    private val deviceModeButton: DeviceModeButton = DeviceModeButton()
    private val deviceModeController: DeviceModeController = DeviceModeController(browser)

    // 主题开关按钮
    private val themeToggleButton: JButton = JButton(AllIcons.General.InspectionsEye)
    // 主题适配器引用（需要从外部设置）
    private var themeAdapter: DarculaThemeAdapter? = null

    // 设置状态管理
    private val settingsState = DevBrowserSettingsState.getInstance(project)

    init {
        border = JBUI.Borders.empty(4, 0)

        // 创建左侧按钮面板（导航按钮）
        val leftButtonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 2, 2))
        leftButtonPanel.add(backButton)
        leftButtonPanel.add(forwardButton)
        leftButtonPanel.add(refreshButton)

        // 创建右侧按钮面板（功能按钮）- 使用 BoxLayout 实现右对齐和精确间隔
        val rightButtonPanel = JPanel()
        rightButtonPanel.layout = BoxLayout(rightButtonPanel, BoxLayout.X_AXIS)

        // 添加按钮，按钮间用 2px 的刚性空白分隔（与左侧一致）
        rightButtonPanel.add(bookmarkActionButton)
        rightButtonPanel.add(Box.createHorizontalStrut(2))
        rightButtonPanel.add(bookmarkListButton)
        rightButtonPanel.add(Box.createHorizontalStrut(2))
        rightButtonPanel.add(deviceModeButton)
        rightButtonPanel.add(Box.createHorizontalStrut(2))
        rightButtonPanel.add(themeToggleButton)

        // 添加到布局
        add(leftButtonPanel, BorderLayout.WEST)
        add(urlField, BorderLayout.CENTER)
        add(rightButtonPanel, BorderLayout.EAST)

        // 设置按钮提示
        backButton.toolTipText = "后退"
        forwardButton.toolTipText = "前进"
        refreshButton.toolTipText = "刷新"
        themeToggleButton.toolTipText = "切换主题适配"

        // 设置按钮样式
        configureButton(backButton)
        configureButton(forwardButton)
        configureButton(refreshButton)
        configureButton(bookmarkActionButton)
        configureButton(bookmarkListButton)
        configureButton(deviceModeButton)
        configureButton(themeToggleButton)

        // 设置书签按钮的回调
        setupBookmarkCallbacks()

        // 设置设备模式按钮的回调
        setupDeviceModeCallback()

        // 初始化按钮状态
        updateButtonStates()
        updateThemeButtonState()

        // 绑定事件监听器
        setupEventListeners()

        // 监听浏览器URL变化
        setupBrowserLoadHandler()
    }

    /**
     * 配置按钮样式
     */
    private fun configureButton(button: JButton) {
        button.preferredSize = JBUI.size(32, 32)
        button.isFocusable = false
        button.isContentAreaFilled = false
        button.border = JBUI.Borders.empty(4)
    }

    /**
     * 设置书签按钮的回调
     */
    private fun setupBookmarkCallbacks() {
        // 当书签变化时，更新星标按钮和列表按钮状态
        bookmarkActionButton.onBookmarkChanged = {
            bookmarkListButton.updateButtonState()
        }

        bookmarkListButton.onBookmarkListChanged = {
            bookmarkActionButton.updateButtonState()
        }
    }

    /**
     * 设置设备模式按钮的回调
     */
    private fun setupDeviceModeCallback() {
        deviceModeButton.onModeChanged = { isMobileMode ->
            if (isMobileMode) {
                deviceModeController.switchToMobileMode()
            } else {
                deviceModeController.switchToPCMode()
            }
        }
    }

    /**
     * 设置事件监听器
     */
    private fun setupEventListeners() {
        // 后退按钮
        backButton.addActionListener {
            if (browser.cefBrowser.canGoBack()) {
                browser.cefBrowser.goBack()
            }
        }

        // 前进按钮
        forwardButton.addActionListener {
            if (browser.cefBrowser.canGoForward()) {
                browser.cefBrowser.goForward()
            }
        }

        // 刷新按钮
        refreshButton.addActionListener {
            browser.cefBrowser.reload()
        }

        // 地址栏回车跳转
        urlField.addActionListener {
            val url = normalizeUrl(urlField.text)
            browser.loadURL(url)
        }

        // 主题开关按钮
        themeToggleButton.addActionListener {
            toggleTheme()
        }

        // 地址栏失去焦点时同步当前URL
        urlField.addFocusListener(object : java.awt.event.FocusAdapter() {
            override fun focusLost(e: java.awt.event.FocusEvent?) {
                if (urlField.text.isEmpty()) {
                    urlField.text = browser.cefBrowser.url
                }
            }
        })
    }

    /**
     * 设置浏览器加载事件监听器
     */
    private fun setupBrowserLoadHandler() {
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadStart(
                browser: CefBrowser?,
                frame: CefFrame?,
                transitionType: org.cef.network.CefRequest.TransitionType?
            ) {
                SwingUtilities.invokeLater {
                    updateButtonStates()
                    bookmarkActionButton.updateButtonState()
                }
            }

            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    SwingUtilities.invokeLater {
                        // 更新地址栏
                        urlField.text = frame.url

                        // 更新按钮状态
                        updateButtonStates()
                        bookmarkActionButton.updateButtonState()
                    }
                }
            }
        }, browser.cefBrowser)
    }

    /**
     * 更新前进/后退按钮的启用状态
     */
    private fun updateButtonStates() {
        backButton.isEnabled = browser.cefBrowser.canGoBack()
        forwardButton.isEnabled = browser.cefBrowser.canGoForward()
    }

    /**
     * 规范化URL
     * 如果用户输入的不是完整URL，自动添加协议
     */
    private fun normalizeUrl(input: String): String {
        val trimmed = input.trim()

        return when {
            // 已经是完整URL
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed

            // 本地文件
            trimmed.startsWith("file://") -> trimmed

            // 看起来是域名，添加https://
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"

            // 否则作为搜索查询（使用Google搜索）
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
        }
    }

    /**
     * 设置地址栏URL
     */
    fun setUrl(url: String) {
        urlField.text = url
    }

    /**
     * 获取当前地址栏URL
     */
    fun getUrl(): String {
        return urlField.text
    }

    /**
     * 设置主题适配器引用
     * 必须在使用前设置
     */
    fun setThemeAdapter(adapter: DarculaThemeAdapter) {
        this.themeAdapter = adapter
        updateThemeButtonState()
    }

    /**
     * 切换主题启用状态
     */
    private fun toggleTheme() {
        val adapter = themeAdapter ?: return

        val newState = !adapter.isThemeEnabled()

        if (newState) {
            adapter.enableTheme()
        } else {
            adapter.disableTheme()
        }

        // 保存设置
        settingsState.state.themeEnabled = newState

        // 更新按钮状态
        updateThemeButtonState()
    }

    /**
     * 更新主题按钮的视觉状态
     */
    private fun updateThemeButtonState() {
        val isEnabled = themeAdapter?.isThemeEnabled() ?: settingsState.state.themeEnabled

        // 通过选中状态区分启用/禁用
        themeToggleButton.isSelected = isEnabled

        // 更新提示文本
        themeToggleButton.toolTipText = if (isEnabled) {
            "主题适配：已启用（点击禁用）"
        } else {
            "主题适配：已禁用（点击启用）"
        }
    }
}
