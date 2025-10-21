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
     * 书签列表
     * 存储用户收藏的网页书签
     */
    var bookmarks: MutableList<Bookmark> = mutableListOf()
)
