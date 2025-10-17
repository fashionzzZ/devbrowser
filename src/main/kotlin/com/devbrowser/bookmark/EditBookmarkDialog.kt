package com.devbrowser.bookmark

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 书签编辑对话框
 * 用于添加或编辑书签
 *
 * @param project 当前项目
 * @param bookmark 要编辑的书签（null表示添加新书签）
 * @param defaultTitle 默认标题
 * @param defaultUrl 默认URL
 */
class EditBookmarkDialog(
    project: Project,
    private val bookmark: Bookmark? = null,
    defaultTitle: String = "",
    defaultUrl: String = ""
) : DialogWrapper(project) {

    private val titleField: JBTextField
    private val urlField: JBTextField

    // 书签管理器
    private val bookmarkManager = BookmarkManager.getInstance(project)

    // 对话框结果
    var resultBookmark: Bookmark? = null
        private set

    init {
        // 初始化输入框
        titleField = JBTextField(bookmark?.title ?: defaultTitle)
        urlField = JBTextField(bookmark?.url ?: defaultUrl)

        // 设置对话框标题
        title = if (bookmark == null) "添加书签" else "编辑书签"

        // 初始化对话框
        init()
    }

    /**
     * 创建对话框中心面板
     */
    override fun createCenterPanel(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("标题:"), titleField, 1, false)
            .addLabeledComponent(JBLabel("URL:"), urlField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        panel.preferredSize = JBUI.size(400, 100)
        return panel
    }

    /**
     * 验证输入
     */
    override fun doValidate(): ValidationInfo? {
        val title = titleField.text.trim()
        val url = urlField.text.trim()

        // 验证标题
        if (title.isBlank()) {
            return ValidationInfo("标题不能为空", titleField)
        }

        // 验证URL
        if (url.isBlank()) {
            return ValidationInfo("URL不能为空", urlField)
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ValidationInfo("URL必须以http://或https://开头", urlField)
        }

        // 如果是添加新书签，检查URL是否已存在
        if (bookmark == null) {
            if (bookmarkManager.isBookmarkExists(url)) {
                return ValidationInfo("该URL已存在书签", urlField)
            }
        } else {
            // 如果是编辑书签，且URL发生变化，检查新URL是否与其他书签冲突
            if (url != bookmark.url && bookmarkManager.isBookmarkExists(url)) {
                return ValidationInfo("该URL已被其他书签使用", urlField)
            }
        }

        return null
    }

    /**
     * 点击确定按钮时的处理
     */
    override fun doOKAction() {
        val title = titleField.text.trim()
        val url = urlField.text.trim()

        if (bookmark == null) {
            // 添加新书签
            resultBookmark = bookmarkManager.addBookmark(title, url)
        } else {
            // 更新现有书签
            val success = bookmarkManager.updateBookmark(bookmark.id, title, url)
            if (success) {
                resultBookmark = bookmarkManager.findBookmarkById(bookmark.id)
            }
        }

        super.doOKAction()
    }

    /**
     * 获取首选焦点组件
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return titleField
    }
}
