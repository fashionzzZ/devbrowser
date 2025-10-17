# DevBrowser

<div align="center">

<img src="src/main/resources/icons/browser.svg" width="128" height="128">

**一个集成在 IntelliJ IDEA 中的开发者友好型浏览器插件**

[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2024.3.3+-blue.svg)](https://www.jetbrains.com/idea/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-17-red.svg)](https://adoptium.net/)

</div>

---

### 📋 项目简介

DevBrowser 是一个轻量级的 IntelliJ IDEA 浏览器插件，为开发者提供无缝集成在 IDE 内的浏览器体验。基于 **JCEF (Java Chromium Embedded Framework)** 实现，支持书签管理、设备模式切换、主题适配等实用功能，让您无需离开 IDE 即可浏览文档、测试网页。

### ✨ 核心特性

- 🌐 **嵌入式 Chromium 浏览器** - 完整的现代网页浏览能力
- 📱 **设备模式切换** - 一键切换 PC/移动端视口和 User-Agent
- ⭐ **书签管理** - 添加、编辑、删除和快速访问常用网站
- 🎨 **Darcula 主题适配** - 网页自动适配 IDE 深色主题，视觉统一
- 💾 **设置持久化** - 自动保存浏览状态、书签和配置

### 🖼️ 功能展示

#### 主界面
```
┌─────────────────────────────────────────────────────────────┐
│  ← → ↻  [https://example.com              ] ⭐📚📱🎨      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│              [ Chromium 浏览器内容区域 ]                    │
│                                                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**工具栏功能说明**：
- `← →` 前进/后退按钮
- `↻` 刷新按钮
- `[ URL ]` 智能地址栏（支持 URL 和搜索）
- `⭐` 添加/删除书签
- `📚` 书签列表
- `📱` PC/移动端切换
- `🎨` Darcula 主题开关

### 🚀 快速开始

#### 开发环境要求

| 组件 | 最低版本 | 推荐版本 |
|------|----------|----------|
| **IntelliJ IDEA** | 2024.3.3 | 最新版 |
| **JDK** | 17 | JetBrains Runtime 17 |
| **Gradle** | 8.5 | 8.5+ |

#### 安装步骤

**方式 1: 从源码构建**

```bash
# 1. 克隆仓库
git clone https://github.com/fashionzzZ/devbrowser.git
cd devbrowser

# 2. 构建插件
./gradlew buildPlugin

# 3. 安装插件
# 插件包位于: build/distributions/devbrowser-1.0.0.zip
# 在 IDEA 中: Settings → Plugins → Install Plugin from Disk → 选择 zip 文件
```

**方式 2: 开发模式运行**

```bash
# 启动带插件的开发 IDE 实例
./gradlew runIde
```

#### 使用指南

1. **打开浏览器面板**
   在 IDEA 右侧工具栏找到 **DevBrowser** 图标，点击打开

2. **浏览网页**
   在地址栏输入 URL 或搜索关键词，按回车键

3. **添加书签**
   浏览到喜欢的网页后，点击 ⭐ 星标按钮

4. **切换移动端模式**
   点击 📱 按钮，浏览器将模拟移动端的视口和 User-Agent

5. **启用深色主题**
   点击 🎨 按钮，网页将自动适配 Darcula 配色

### 📂 项目结构

```
devbrowser/
├── src/main/kotlin/com/devbrowser/
│   ├── DevBrowserToolWindowFactory.kt  # 插件入口
│   ├── ui/                              # UI 层
│   │   ├── DevBrowserPanel.kt          # 主面板
│   │   └── BrowserToolbar.kt           # 工具栏
│   ├── bookmark/                        # 书签模块
│   │   ├── BookmarkManager.kt          # 业务逻辑
│   │   ├── Bookmark.kt                 # 数据模型
│   │   └── [UI组件...]
│   ├── device/                          # 设备模式模块
│   │   ├── DeviceModeController.kt     # 模式控制器
│   │   ├── UserAgentHandler.kt         # UA 处理
│   │   ├── MobileViewportAdapter.kt    # 视口适配
│   │   └── MobileDevice.kt             # 设备配置
│   ├── theme/                           # 主题模块
│   │   └── DarculaThemeAdapter.kt      # CSS 注入
│   └── settings/                        # 设置模块
│       ├── DevBrowserSettings.kt       # 数据类
│       └── DevBrowserSettingsState.kt  # 持久化
└── src/main/resources/
    └── META-INF/
        └── plugin.xml                   # 插件配置
```

### 🛠️ 技术栈

- **语言**: Kotlin 1.9.22
- **平台**: IntelliJ Platform SDK 2024.3.3
- **浏览器引擎**: JCEF (Chromium Embedded Framework)
- **构建工具**: Gradle 8.5 + Gradle IntelliJ Plugin
- **UI 框架**: Swing (BorderLayout, FlowLayout)

### 🎯 设计原则

本项目严格遵循 **SOLID 原则**：

- **单一职责 (SRP)**: 每个模块职责明确（如 BookmarkManager 仅管理书签）
- **开闭原则 (OCP)**: 通过扩展支持新功能（如添加新设备配置）
- **依赖倒置 (DIP)**: 依赖 IntelliJ Platform 抽象接口，而非具体实现

### 🐛 已知问题

1. **JCEF 不支持**
   某些平台或 JDK 版本不支持 JCEF，请使用 JetBrains Runtime JDK

2. **窗口透明度**
   某些操作系统不支持窗口透明度功能（功能会静默失败）

3. **内联样式覆盖**
   网页使用内联 `style` 属性时，Darcula 主题可能无法完全覆盖

### 🗺️ 路线图

#### 高优先级
- [ ] 添加单元测试和集成测试
- [ ] 实现多标签页支持
- [ ] 集成开发者工具（Console、Network）

#### 中优先级
- [ ] 支持更多移动设备（iPad、Android）
- [ ] 书签导入/导出功能（JSON 格式）
- [ ] 浏览历史记录

#### 低优先级
- [ ] 国际化支持（i18n）
- [ ] 性能监控面板
- [ ] 插件配置页面

### 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

**代码规范**：
- 遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- 添加必要的中文注释
- 提交前运行 `./gradlew check`

### 📄 许可证

本项目采用 **MIT License** 开源许可证。详见 [LICENSE](LICENSE) 文件。

### 🙏 致谢

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [JCEF](https://github.com/chromiumembedded/java-cef)
- [Kotlin](https://kotlinlang.org/)

### 📮 联系方式

- **问题反馈**: [GitHub Issues](https://github.com/fashionzzZ/devbrowser/issues)
- **讨论**: [GitHub Discussions](https://github.com/fashionzzZ/devbrowser/discussions)
