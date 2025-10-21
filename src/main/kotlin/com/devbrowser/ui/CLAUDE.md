# UI 模块

> **路径**: [`/CLAUDE.md`](../../../../../CLAUDE.md) → `src/main/kotlin/com/devbrowser/ui/`

---

## 📋 模块概述

UI 模块负责 DevBrowser 插件的用户界面呈现和交互控制，提供主面板和浏览器工具栏两个核心组件。

---

## 🗂️ 模块文件清单

| 文件名 | 行数 | 职责 | 入口点 |
|--------|------|------|--------|
| **DevBrowserPanel.kt** | ~120 | 主浏览器面板容器 | `init:39`, `initBrowser():54` |
| **BrowserToolbar.kt** | ~250 | 浏览器控制工具栏 | `init:48`, `setupEventListeners():140` |

---

## 🏗️ 组件架构

```
DevBrowserPanel (BorderLayout)
├── [NORTH] BrowserToolbar
│   ├── [WEST] 导航按钮面板
│   │   ├── backButton
│   │   ├── forwardButton
│   │   └── refreshButton
│   ├── [CENTER] urlField (地址栏)
│   └── [EAST] 功能按钮面板
│       ├── bookmarkActionButton (添加/删除书签)
│       ├── bookmarkListButton (书签列表)
│       └── deviceModeButton (PC/移动切换)
└── [CENTER] JBCefBrowser (JCEF 浏览器)
```

---

## 📄 组件详解

### 1. DevBrowserPanel

**职责**: 主容器面板，管理浏览器实例和工具栏的生命周期

**核心依赖**:
```kotlin
- JBCefBrowser (JCEF 浏览器引擎)
- DevBrowserSettingsState (设置持久化)
- BrowserToolbar (工具栏)
```

**关键方法**:
- `initBrowser(): Unit` (54行) - 初始化 JCEF 浏览器实例
- `showUnsupportedMessage(): Unit` (82行) - JCEF 不支持时显示提示
- `dispose(): Unit` (110行) - 资源清理和状态保存

**生命周期**:
```
创建 → 检查JCEF支持 → 初始化浏览器 → 创建工具栏
     → 加载保存的URL → 运行中 → dispose保存状态
```

**JCEF 支持检查**:
```kotlin
if (JBCefApp.isSupported()) {
    initBrowser()
} else {
    showUnsupportedMessage()
}
```

---

### 2. BrowserToolbar

**职责**: 提供浏览器控制UI和功能集成

**核心依赖**:
```kotlin
- JBCefBrowser (浏览器实例)
- BookmarkActionButton & BookmarkListButton (书签模块)
- DeviceModeButton & DeviceModeController (设备模块)
```

**布局策略**:
- **WEST**: 导航按钮（FlowLayout，左对齐）
- **CENTER**: 地址栏（自动拉伸）
- **EAST**: 功能按钮（BoxLayout，2px 间隔）

**核心功能实现**:

**1) 智能 URL 处理** (230行):
```kotlin
normalizeUrl(input: String): String
- 完整URL → 直接使用
- 域名 → 添加 https://
- 其他 → Google 搜索
```

**2) 浏览器状态监听** (180行):
```kotlin
setupBrowserLoadHandler() {
    onLoadStart → 更新按钮状态
    onLoadEnd   → 同步地址栏 + 更新书签按钮
}
```

**3) 书签回调协调** (120行):
```kotlin
bookmarkActionButton.onBookmarkChanged → 更新列表按钮
bookmarkListButton.onBookmarkListChanged → 更新星标按钮
```

---

## 🔗 模块间依赖

**依赖的模块**:
- `../bookmark/` - 书签管理和 UI 组件
- `../device/` - 设备模式切换
- `../settings/` - 设置持久化

**被依赖方**:
- 被 `DevBrowserToolWindowFactory` 创建和管理

---

## 📊 数据流

```
用户输入URL → normalizeUrl() → browser.loadURL()
                                    ↓
                          CefLoadHandler.onLoadStart
                                    ↓
                          更新按钮状态 + 书签按钮
                                    ↓
                          CefLoadHandler.onLoadEnd
                                    ↓
                          同步地址栏 + 最终状态
```

---

## 🎨 UI 规范

### 按钮样式配置
```kotlin
preferredSize = 32x32
isFocusable = false
isContentAreaFilled = false
border = 4px empty
```

### 边距标准
- Panel: 8px 外边距
- Toolbar: 4px 上下边距
- 按钮间隔: 2px

---

## ⚠️ 关键注意事项

1. **JCEF 依赖**: 必须使用 JetBrains Runtime JDK
2. **资源清理**: `dispose()` 必须保存当前 URL 并释放浏览器
3. **线程安全**: 所有 UI 更新必须在 Swing 线程 (`SwingUtilities.invokeLater`)

---

## 🧪 测试建议

### 单元测试用例
- [ ] URL 规范化逻辑（normalizeUrl）
- [ ] 前进/后退按钮状态更新

### 集成测试用例
- [ ] JCEF 不支持时的降级提示
- [ ] 浏览器加载事件监听
- [ ] 书签按钮联动更新

---

## 📈 性能考量

- 浏览器加载事件频繁触发，避免在 `onLoadStart/End` 中执行重计算
- 地址栏 URL 同步使用节流，避免过度刷新

---

## 🔮 扩展点

- [ ] 添加标签页支持（多 JBCefBrowser 实例管理）
- [ ] 工具栏自定义配置（用户可隐藏/显示按钮）
- [ ] 地址栏历史记录下拉提示
