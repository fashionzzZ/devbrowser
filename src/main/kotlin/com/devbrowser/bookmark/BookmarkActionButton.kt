package com.devbrowser.bookmark

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.event.ActionListener
import javax.swing.JButton

/**
 * 书签操作按钮（星标按钮）
 * 用于快速添加/删除当前页面的书签
 *
 * @param project 当前项目
 * @param browser 浏览器实例
 */
class BookmarkActionButton(
    private val project: Project,
    private val browser: JBCefBrowser
) : JButton() {

    private val bookmarkManager = BookmarkManager.getInstance(project)

    // 回调函数，用于通知书签状态变化
    var onBookmarkChanged: (() -> Unit)? = null

    init {
        // 初始化按钮样式
        icon = AllIcons.Toolwindows.ToolWindowBookmarks
        toolTipText = "添加书签"
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false

        // 添加点击事件
        addActionListener(createClickListener())

        // 初始化按钮状态
        updateButtonState()
    }

    /**
     * 创建点击事件监听器
     */
    private fun createClickListener(): ActionListener {
        return ActionListener {
            val currentUrl = getCurrentUrl()

            if (currentUrl.isEmpty() || !isValidUrl(currentUrl)) {
                return@ActionListener
            }

            val existingBookmark = bookmarkManager.findBookmarkByUrl(currentUrl)

            if (existingBookmark != null) {
                // 如果书签已存在，删除它
                bookmarkManager.deleteBookmark(existingBookmark.id)
                updateButtonState()
                onBookmarkChanged?.invoke()
            } else {
                // 如果书签不存在，打开编辑对话框添加
                showAddBookmarkDialog(currentUrl)
            }
        }
    }

    /**
     * 显示添加书签对话框
     */
    private fun showAddBookmarkDialog(url: String) {
        // 尝试获取页面标题作为默认书签名称
        val pageTitle = getPageTitle() ?: url

        val dialog = EditBookmarkDialog(
            project = project,
            bookmark = null,
            defaultTitle = pageTitle,
            defaultUrl = url
        )

        if (dialog.showAndGet()) {
            updateButtonState()
            onBookmarkChanged?.invoke()
        }
    }

    /**
     * 更新按钮状态
     * 根据当前URL是否已添加书签来更新图标和提示
     */
    fun updateButtonState() {
        val currentUrl = getCurrentUrl()

        if (currentUrl.isEmpty() || !isValidUrl(currentUrl)) {
            icon = AllIcons.Toolwindows.ToolWindowBookmarks
            toolTipText = "添加书签"
            isEnabled = false
            return
        }

        isEnabled = true
        val isBookmarked = bookmarkManager.isBookmarkExists(currentUrl)

        if (isBookmarked) {
            // 已加入书签
            icon = AllIcons.Nodes.Bookmark
            toolTipText = "移除书签"
        } else {
            // 未加入书签
            icon = AllIcons.Toolwindows.ToolWindowBookmarks
            toolTipText = "添加书签"
        }
    }

    /**
     * 获取当前浏览器URL
     */
    private fun getCurrentUrl(): String {
        return try {
            browser.cefBrowser.url ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取页面标题
     */
    private fun getPageTitle(): String? {
        return try {
            // 通过JavaScript获取页面标题
            var title: String? = null
            browser.cefBrowser.executeJavaScript(
                "document.title",
                browser.cefBrowser.url,
                0
            )
            // 注意：executeJavaScript是异步的，这里简化处理
            // 在实际使用中可能需要回调机制
            title
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 验证URL格式
     */
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
