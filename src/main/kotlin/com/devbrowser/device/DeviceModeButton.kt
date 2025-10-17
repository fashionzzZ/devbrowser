package com.devbrowser.device

import com.intellij.icons.AllIcons
import javax.swing.JButton

/**
 * 设备模式切换按钮
 * 用于在 PC 模式和移动端模式之间切换
 */
class DeviceModeButton : JButton() {

    // 回调函数，当模式切换时触发
    var onModeChanged: ((Boolean) -> Unit)? = null

    // 当前模式状态（true = 移动端，false = PC）
    private var isMobileMode: Boolean = false

    init {
        // 初始化按钮样式
        icon = AllIcons.Actions.PreviewDetails
        toolTipText = "切换到移动端模式"
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false

        // 添加点击事件
        addActionListener {
            toggleMode()
        }
    }

    /**
     * 切换模式
     */
    private fun toggleMode() {
        isMobileMode = !isMobileMode
        updateButtonState()
        onModeChanged?.invoke(isMobileMode)
    }

    /**
     * 设置当前模式
     * @param mobileMode true = 移动端模式，false = PC 模式
     */
    fun setMode(mobileMode: Boolean) {
        if (this.isMobileMode != mobileMode) {
            this.isMobileMode = mobileMode
            updateButtonState()
        }
    }

    /**
     * 获取当前模式
     */
    fun isMobileMode(): Boolean {
        return isMobileMode
    }

    /**
     * 更新按钮状态
     */
    private fun updateButtonState() {
        if (isMobileMode) {
            // 移动端模式
            icon = AllIcons.Actions.PreviewDetailsVertically
            toolTipText = "移动端模式 - 点击切换到 PC 模式"
            isSelected = true
        } else {
            // PC 模式
            icon = AllIcons.Actions.PreviewDetails
            toolTipText = "PC 模式 - 点击切换到移动端模式"
            isSelected = false
        }
    }
}
