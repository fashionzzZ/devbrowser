package com.devbrowser.settings

import com.devbrowser.bookmark.Bookmark

/**
 * DevBrowser设置数据类
 * 存储用户的配置信息
 */
data class DevBrowserSettings(
    /**
     * 最后访问的URL
     */
    var lastUrl: String = "https://www.google.com",

    /**
     * 是否启用Darcula主题适配
     * 默认关闭，用户可通过工具栏按钮控制
     */
    var themeEnabled: Boolean = false,

    /**
     * 书签列表
     * 存储用户收藏的网页书签
     */
    var bookmarks: MutableList<Bookmark> = mutableListOf()
)
