package com.devbrowser.bookmark

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.event.ActionListener
import javax.swing.JButton

/**
 * 书签列表按钮
 * 点击后显示书签弹出菜单
 *
 * @param project 当前项目
 * @param browser 浏览器实例
 */
class BookmarkListButton(
    private val project: Project,
    private val browser: JBCefBrowser
) : JButton() {

    private val bookmarkManager = BookmarkManager.getInstance(project)
    private var popupMenu: BookmarkPopupMenu? = null

    // 回调函数，用于在书签列表更新后刷新
    var onBookmarkListChanged: (() -> Unit)? = null

    init {
        // 初始化按钮样式
        icon = AllIcons.Nodes.BookmarkGroup
        toolTipText = "书签列表"
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
            showBookmarkPopup()
        }
    }

    /**
     * 显示书签弹出菜单
     */
    private fun showBookmarkPopup() {
        val bookmarks = bookmarkManager.getAllBookmarks()

        if (bookmarks.isEmpty()) {
            // 如果没有书签，显示提示
            toolTipText = "暂无书签"
            return
        }

        // 创建或更新弹出菜单
        if (popupMenu == null) {
            popupMenu = BookmarkPopupMenu(project, browser)
            popupMenu?.onBookmarkUpdated = {
                updateButtonState()
                onBookmarkListChanged?.invoke()
            }
        }

        // 显示弹出菜单
        popupMenu?.show(this, 0, height)
    }

    /**
     * 更新按钮状态
     */
    fun updateButtonState() {
        val count = bookmarkManager.getBookmarkCount()

        if (count == 0) {
            toolTipText = "书签列表（暂无书签）"
            isEnabled = true  // 仍然可点击，以便用户看到"暂无书签"提示
        } else {
            toolTipText = "书签列表（$count 个）"
            isEnabled = true
        }

        // 刷新弹出菜单
        popupMenu?.refreshBookmarks()
    }

    /**
     * 强制刷新书签列表
     */
    fun refresh() {
        updateButtonState()
    }
}
