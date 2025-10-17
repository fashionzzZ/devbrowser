package com.devbrowser.bookmark

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.event.ActionListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JSeparator

/**
 * 书签弹出菜单
 * 显示所有书签，并提供编辑/删除功能
 *
 * @param project 当前项目
 * @param browser 浏览器实例
 */
class BookmarkPopupMenu(
    private val project: Project,
    private val browser: JBCefBrowser
) : JPopupMenu() {

    private val bookmarkManager = BookmarkManager.getInstance(project)

    // 书签更新回调
    var onBookmarkUpdated: (() -> Unit)? = null

    init {
        refreshBookmarks()
    }

    /**
     * 刷新书签列表
     */
    fun refreshBookmarks() {
        removeAll()

        val bookmarks = bookmarkManager.getAllBookmarks()

        if (bookmarks.isEmpty()) {
            // 显示空状态提示
            val emptyItem = JMenuItem("暂无书签")
            emptyItem.isEnabled = false
            add(emptyItem)
            return
        }

        // 按创建时间倒序排列（最新的在前）
        val sortedBookmarks = bookmarks.sortedByDescending { it.createdAt }

        // 限制显示数量，避免菜单过长
        val maxDisplayCount = 20
        val displayBookmarks = sortedBookmarks.take(maxDisplayCount)

        // 添加书签项
        displayBookmarks.forEach { bookmark ->
            add(createBookmarkMenuItem(bookmark))
        }

        // 如果书签数量超过限制，显示提示
        if (sortedBookmarks.size > maxDisplayCount) {
            add(JSeparator())
            val moreItem = JMenuItem("还有 ${sortedBookmarks.size - maxDisplayCount} 个书签...")
            moreItem.isEnabled = false
            add(moreItem)
        }
    }

    /**
     * 创建单个书签菜单项
     */
    private fun createBookmarkMenuItem(bookmark: Bookmark): JMenuItem {
        val menuItem = JMenuItem(bookmark.title)
        menuItem.icon = AllIcons.Nodes.Bookmark
        menuItem.toolTipText = bookmark.url

        // 主点击事件：跳转到书签URL
        menuItem.addActionListener {
            browser.loadURL(bookmark.url)
        }

        // 右键菜单：编辑和删除
        val contextMenu = JPopupMenu()

        // 编辑菜单项
        val editItem = JMenuItem("编辑", AllIcons.Actions.Edit)
        editItem.addActionListener(createEditActionListener(bookmark))
        contextMenu.add(editItem)

        // 删除菜单项
        val deleteItem = JMenuItem("删除", AllIcons.Actions.Close)
        deleteItem.addActionListener(createDeleteActionListener(bookmark))
        contextMenu.add(deleteItem)

        // 绑定右键菜单
        menuItem.componentPopupMenu = contextMenu

        return menuItem
    }

    /**
     * 创建编辑操作监听器
     */
    private fun createEditActionListener(bookmark: Bookmark): ActionListener {
        return ActionListener {
            val dialog = EditBookmarkDialog(
                project = project,
                bookmark = bookmark
            )

            if (dialog.showAndGet()) {
                refreshBookmarks()
                onBookmarkUpdated?.invoke()
            }
        }
    }

    /**
     * 创建删除操作监听器
     */
    private fun createDeleteActionListener(bookmark: Bookmark): ActionListener {
        return ActionListener {
            // 显示确认对话框
            val result = Messages.showYesNoDialog(
                project,
                "确定要删除书签 \"${bookmark.title}\" 吗？",
                "删除书签",
                "删除",
                "取消",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                bookmarkManager.deleteBookmark(bookmark.id)
                refreshBookmarks()
                onBookmarkUpdated?.invoke()
            }
        }
    }
}
