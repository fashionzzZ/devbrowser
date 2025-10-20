# DevBrowser - IntelliJ IDEA 浏览器插件

> **最后更新**: 2025-10-20 (Version 1.1.0)
> **项目类型**: IntelliJ IDEA Plugin (Kotlin)
> **构建工具**: Gradle 8.10 + IntelliJ Platform Gradle Plugin 2.2.1
> **目标平台**: IntelliJ IDEA 2024.3+ ~ 2025.2+ (Build 243 ~ 252.*)

---

## 📋 项目概述

DevBrowser 是一个轻量级的 IntelliJ IDEA 浏览器插件，为开发者提供集成在 IDE 内的浏览器体验。基于 JCEF (Java Chromium Embedded Framework) 实现，支持书签管理、设备模式切换和主题适配等功能。

### 核心特性
- 🌐 **嵌入式 Chromium 浏览器** - 完整的网页浏览能力
- 📱 **设备模式切换** - PC/移动端视口和 User-Agent 模拟
- ⭐ **书签管理** - 添加、编辑、删除和快速访问书签
- 🎨 **Darcula 主题适配** - 与 IDE 深色主题同步
- 💾 **设置持久化** - 自动保存浏览状态和配置

---

## 🏗️ 项目架构

```mermaid
graph TB
    subgraph "Plugin Entry"
        Factory[DevBrowserToolWindowFactory<br/>工具窗口工厂]
    end

    subgraph "UI Layer - src/main/kotlin/com/devbrowser/ui"
        Panel[DevBrowserPanel<br/>主面板 - BorderLayout]
        Toolbar[BrowserToolbar<br/>工具栏]
    end

    subgraph "Core Features"
        subgraph "Bookmark Module - bookmark/"
            BookmarkMgr[BookmarkManager<br/>@Service - 业务逻辑]
            BookmarkModel[Bookmark<br/>数据模型]
            BookmarkUI[BookmarkUI组件<br/>PopupMenu/Button/Dialog]
        end

        subgraph "Device Module - device/"
            DeviceCtrl[DeviceModeController<br/>设备模式协调器]
            UAHandler[UserAgentHandler<br/>User-Agent处理]
            Viewport[MobileViewportAdapter<br/>视口适配]
            DeviceModel[MobileDevice<br/>设备配置]
        end

        subgraph "Theme Module - theme/"
            ThemeAdapter[DarculaThemeAdapter<br/>主题CSS注入]
        end

        subgraph "Settings - settings/"
            SettingsState[DevBrowserSettingsState<br/>@Service - 持久化]
            SettingsData[DevBrowserSettings<br/>数据类]
        end
    end

    subgraph "IntelliJ Platform"
        JCEF[JBCefBrowser<br/>JCEF封装]
        PersistentAPI[PersistentStateComponent<br/>持久化API]
        ServiceAPI[Service API]
    end

    Factory --> Panel
    Panel --> Toolbar
    Panel --> JCEF
    Toolbar --> BookmarkMgr
    Toolbar --> DeviceCtrl
    Toolbar --> ThemeAdapter

    DeviceCtrl --> UAHandler
    DeviceCtrl --> Viewport
    DeviceCtrl --> DeviceModel
    Viewport --> JCEF
    UAHandler --> JCEF

    ThemeAdapter --> JCEF

    BookmarkMgr --> SettingsState
    BookmarkMgr --> BookmarkModel
    BookmarkUI --> BookmarkMgr
    Toolbar --> BookmarkUI

    Panel --> SettingsState
    SettingsState --> SettingsData
    SettingsState -.implements.-> PersistentAPI
    BookmarkMgr -.implements.-> ServiceAPI

    classDef entry fill:#e1f5ff,stroke:#01579b
    classDef ui fill:#f3e5f5,stroke:#4a148c
    classDef service fill:#e8f5e9,stroke:#1b5e20
    classDef model fill:#fff3e0,stroke:#e65100
    classDef platform fill:#fce4ec,stroke:#880e4f

    class Factory entry
    class Panel,Toolbar ui
    class BookmarkMgr,DeviceCtrl,ThemeAdapter,SettingsState service
    class BookmarkModel,DeviceModel,SettingsData model
    class JCEF,PersistentAPI,ServiceAPI platform
```

### 架构分层说明

**1. 入口层 (Entry)**
- `DevBrowserToolWindowFactory`: 工具窗口注册和创建入口

**2. UI 层 (UI Layer)**
- `DevBrowserPanel`: 主容器，管理浏览器和工具栏布局
- `BrowserToolbar`: 导航控制、书签按钮、设备切换、主题切换

**3. 业务层 (Core Features)**
- **Bookmark 模块**: 书签业务逻辑和 UI 组件
- **Device 模块**: 设备模式切换、User-Agent 和视口管理
- **Theme 模块**: Darcula 主题 CSS 注入
- **Settings 模块**: 配置数据持久化

**4. 平台层 (Platform)**
- IntelliJ Platform SDK 提供的基础能力

---

## 📂 模块索引

| 模块 | 路径 | 职责 | 详细文档 |
|------|------|------|----------|
| **UI 模块** | `src/main/kotlin/com/devbrowser/ui/` | 用户界面组件 | [UI/CLAUDE.md](src/main/kotlin/com/devbrowser/ui/CLAUDE.md) |
| **书签模块** | `src/main/kotlin/com/devbrowser/bookmark/` | 书签管理业务逻辑 | [bookmark/CLAUDE.md](src/main/kotlin/com/devbrowser/bookmark/CLAUDE.md) |
| **设备模块** | `src/main/kotlin/com/devbrowser/device/` | 设备模式切换 | [device/CLAUDE.md](src/main/kotlin/com/devbrowser/device/CLAUDE.md) |
| **主题模块** | `src/main/kotlin/com/devbrowser/theme/` | Darcula 主题适配 | [theme/CLAUDE.md](src/main/kotlin/com/devbrowser/theme/CLAUDE.md) |
| **设置模块** | `src/main/kotlin/com/devbrowser/settings/` | 配置持久化 | [settings/CLAUDE.md](src/main/kotlin/com/devbrowser/settings/CLAUDE.md) |

---

## 🛠️ 技术栈

| 组件 | 版本/类型 | 用途 |
|------|-----------|------|
| **语言** | Kotlin 2.2.0 | 主开发语言 (支持 K2 编译器) |
| **JVM** | Java 17 (推荐 21) | 运行时环境 |
| **平台** | IntelliJ Platform SDK 2025.2.3 | 插件框架 |
| **浏览器引擎** | JCEF (JBCefBrowser) | Chromium 内核 |
| **构建工具** | Gradle 8.10 + IntelliJ Platform Gradle Plugin 2.x | 构建和打包 |
| **布局管理** | Swing (BorderLayout, FlowLayout) | UI 布局 |

---

## 🎯 开发规范

### SOLID 原则体现

**单一职责 (SRP)**
- 每个模块职责明确：BookmarkManager 只管书签，DeviceModeController 只管设备切换
- UI 组件和业务逻辑分离

**开闭原则 (OCP)**
- 通过接口扩展设备配置（MobileDevice），无需修改控制器
- 主题适配器可扩展支持新主题

**依赖倒置 (DIP)**
- 业务层依赖 IntelliJ Platform 抽象接口（Service、PersistentStateComponent）
- 不直接依赖具体实现

### 代码风格
- **命名**: 使用清晰的中英文混合注释（代码英文，注释中文）
- **空安全**: 充分利用 Kotlin null-safety
- **资源管理**: 实现 Disposable 接口管理生命周期

### 测试策略
- 当前无单元测试（待补充）
- 通过 `./gradlew runIde` 进行手动集成测试

---

## 🚀 快速开始

### 开发环境要求
- IntelliJ IDEA 2024.3+ 或 2025.2+
- JDK 17 (系统最低要求) 或 JDK 21 (官方推荐)
- Gradle 8.10+ (由 Wrapper 自动管理)

### 构建命令
```bash
# 构建插件
./gradlew buildPlugin

# 运行开发环境
./gradlew runIde

# 生成插件包 (位于 build/distributions/)
./gradlew buildPlugin
```

### 插件配置文件
- **plugin.xml**: `src/main/resources/META-INF/plugin.xml`
- **构建配置**: `build.gradle.kts`
- **项目设置**: `gradle.properties`

---

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| **源文件总数** | 19 个 |
| **Kotlin 文件** | 17 个 |
| **资源文件** | 2 个 (plugin.xml + icons) |
| **模块数量** | 5 个功能模块 |
| **代码覆盖率** | ~95% (已扫描所有核心模块) |

---

## 🔍 关键入口点

| 文件 | 作用 | 路径 |
|------|------|------|
| **插件入口** | ToolWindow 工厂 | `src/main/kotlin/com/devbrowser/DevBrowserToolWindowFactory.kt:19` |
| **主面板初始化** | JCEF 浏览器创建 | `src/main/kotlin/com/devbrowser/ui/DevBrowserPanel.kt:54` |
| **书签服务** | 书签增删改查 | `src/main/kotlin/com/devbrowser/bookmark/BookmarkManager.kt:16` |
| **设备切换** | 模式切换协调 | `src/main/kotlin/com/devbrowser/device/DeviceModeController.kt:53` |
| **设置持久化** | PersistentStateComponent | `src/main/kotlin/com/devbrowser/settings/DevBrowserSettingsState.kt` |

---

## 📝 待办事项和改进建议

### 高优先级
- [ ] 添加单元测试和集成测试（使用 JUnit 5 + Mockito）
- [ ] 实现多标签页支持（当前只支持单浏览器实例）
- [ ] 添加开发者工具集成（Console、Network）

### 中优先级
- [ ] 支持更多移动设备预设（当前仅 iPhone 14）
- [ ] 书签导入/导出功能
- [ ] 浏览历史记录

### 低优先级
- [ ] 国际化支持（i18n）
- [ ] 性能监控和优化
- [ ] 插件配置页面（Settings/Preferences 集成）

---

## 🔗 相关资源

- [IntelliJ Platform SDK 文档](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [JCEF GitHub 仓库](https://github.com/chromiumembedded/java-cef)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)

---

## 📄 许可证

MIT License

---

**注意事项**:
- JCEF 需要使用 JetBrains Runtime JDK，某些平台可能不支持
- 插件 ID `com.fashion.devbrowser` 在发布后不能更改
- **版本支持**: IntelliJ IDEA 2024.3+ ~ 2025.2+ (Build 243 ~ 252.*)
