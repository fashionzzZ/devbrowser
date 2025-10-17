package com.devbrowser

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.devbrowser.ui.DevBrowserPanel

/**
 * ToolWindow工厂类
 * 负责创建和初始化DevBrowser工具窗口
 */
class DevBrowserToolWindowFactory : ToolWindowFactory {

    /**
     * 创建ToolWindow内容
     * 当用户首次打开工具窗口时被调用
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // 创建主浏览器面板
        val browserPanel = DevBrowserPanel(project, toolWindow)

        // 创建内容并添加到ToolWindow
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(browserPanel, "", false)

        // 添加内容到内容管理器
        toolWindow.contentManager.addContent(content)
    }

    /**
     * 配置ToolWindow是否应该在项目打开时立即初始化
     * 返回false表示延迟初始化，节省启动时间
     */
    override fun shouldBeAvailable(project: Project): Boolean = true
}
