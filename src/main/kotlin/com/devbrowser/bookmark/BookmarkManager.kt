package com.devbrowser.bookmark

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.devbrowser.settings.DevBrowserSettingsState

/**
 * 书签管理服务
 * 提供书签的增删改查功能
 *
 * 遵循SOLID原则：
 * - 单一职责：仅负责书签业务逻辑
 * - 依赖倒置：依赖抽象的设置状态接口
 */
@Service(Service.Level.PROJECT)
class BookmarkManager(private val project: Project) {

    private val settingsState = DevBrowserSettingsState.getInstance(project)

    /**
     * 获取所有书签
     * @return 书签列表的不可变副本
     */
    fun getAllBookmarks(): List<Bookmark> {
        return settingsState.state.bookmarks.toList()
    }

    /**
     * 添加新书签
     * @param title 书签标题
     * @param url 书签URL
     * @return 添加成功返回新书签，失败返回null
     */
    fun addBookmark(title: String, url: String): Bookmark? {
        val bookmark = Bookmark(title = title, url = url)

        // 验证数据有效性
        if (!bookmark.isValid()) {
            return null
        }

        // 检查是否已存在相同URL的书签
        if (isBookmarkExists(url)) {
            return null
        }

        // 添加到列表
        settingsState.state.bookmarks.add(bookmark)
        return bookmark
    }

    /**
     * 更新书签
     * @param id 书签ID
     * @param newTitle 新标题
     * @param newUrl 新URL
     * @return 更新成功返回true
     */
    fun updateBookmark(id: String, newTitle: String, newUrl: String): Boolean {
        val index = settingsState.state.bookmarks.indexOfFirst { it.id == id }
        if (index == -1) {
            return false
        }

        val oldBookmark = settingsState.state.bookmarks[index]
        val updatedBookmark = oldBookmark.copy(newTitle = newTitle, newUrl = newUrl)

        // 验证更新后的数据
        if (!updatedBookmark.isValid()) {
            return false
        }

        // 如果URL发生变化，检查新URL是否与其他书签冲突
        if (newUrl != oldBookmark.url && isBookmarkExists(newUrl)) {
            return false
        }

        settingsState.state.bookmarks[index] = updatedBookmark
        return true
    }

    /**
     * 删除书签
     * @param id 书签ID
     * @return 删除成功返回true
     */
    fun deleteBookmark(id: String): Boolean {
        return settingsState.state.bookmarks.removeIf { it.id == id }
    }

    /**
     * 根据ID查找书签
     * @param id 书签ID
     * @return 找到返回书签，否则返回null
     */
    fun findBookmarkById(id: String): Bookmark? {
        return settingsState.state.bookmarks.find { it.id == id }
    }

    /**
     * 根据URL查找书签
     * @param url 书签URL
     * @return 找到返回书签，否则返回null
     */
    fun findBookmarkByUrl(url: String): Bookmark? {
        return settingsState.state.bookmarks.find { it.url == url }
    }

    /**
     * 检查URL是否已存在书签
     * @param url 待检查的URL
     * @return 存在返回true
     */
    fun isBookmarkExists(url: String): Boolean {
        return settingsState.state.bookmarks.any { it.url == url }
    }

    /**
     * 获取书签数量
     */
    fun getBookmarkCount(): Int {
        return settingsState.state.bookmarks.size
    }

    /**
     * 清空所有书签
     * 谨慎使用，建议添加确认对话框
     */
    fun clearAllBookmarks() {
        settingsState.state.bookmarks.clear()
    }

    companion object {
        /**
         * 获取BookmarkManager实例
         */
        fun getInstance(project: Project): BookmarkManager {
            return project.getService(BookmarkManager::class.java)
        }
    }
}
