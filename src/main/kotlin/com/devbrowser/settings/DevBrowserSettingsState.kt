package com.devbrowser.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * DevBrowser设置状态管理
 * 负责设置的持久化存储
 */
@Service(Service.Level.PROJECT)
@State(
    name = "DevBrowserSettings",
    storages = [Storage("DevBrowserSettings.xml")]
)
class DevBrowserSettingsState : PersistentStateComponent<DevBrowserSettings> {

    // 设置数据
    private var settings = DevBrowserSettings()

    /**
     * 获取当前设置状态
     * IDEA会调用此方法来序列化设置
     */
    override fun getState(): DevBrowserSettings {
        return settings
    }

    /**
     * 加载设置状态
     * IDEA会在启动时调用此方法来恢复设置
     */
    override fun loadState(state: DevBrowserSettings) {
        settings = state
    }

    companion object {
        /**
         * 获取设置状态实例
         */
        fun getInstance(project: Project): DevBrowserSettingsState {
            return project.getService(DevBrowserSettingsState::class.java)
        }
    }
}
