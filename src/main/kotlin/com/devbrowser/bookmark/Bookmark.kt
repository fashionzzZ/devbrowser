package com.devbrowser.bookmark

import java.util.UUID

/**
 * 书签数据类
 *
 * @property id 唯一标识符（UUID）
 * @property title 书签标题
 * @property url 书签URL
 * @property createdAt 创建时间戳（毫秒）
 */
data class Bookmark(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var url: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 无参构造函数（用于XML反序列化）
     * IntelliJ的序列化器需要这个构造函数
     */
    constructor() : this(
        id = UUID.randomUUID().toString(),
        title = "",
        url = "",
        createdAt = System.currentTimeMillis()
    )
    /**
     * 验证书签数据是否有效
     */
    fun isValid(): Boolean {
        return title.isNotBlank() && url.isNotBlank() && isValidUrl(url)
    }

    /**
     * 简单的URL验证
     */
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * 创建书签的副本（用于编辑）
     */
    fun copy(
        newTitle: String = this.title,
        newUrl: String = this.url
    ): Bookmark {
        return Bookmark(
            id = this.id,
            title = newTitle,
            url = newUrl,
            createdAt = this.createdAt
        )
    }
}
