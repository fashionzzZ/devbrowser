# 设置模块

> **路径**: [`/CLAUDE.md`](../../../../../CLAUDE.md) → `src/main/kotlin/com/devbrowser/settings/`

---

## 📋 模块概述

设置模块负责插件配置的持久化存储，使用 IntelliJ Platform 的 `PersistentStateComponent` 接口实现自动序列化和反序列化。

---

## 🗂️ 模块文件清单

| 文件名 | 行数 | 职责 | 类型 |
|--------|------|------|------|
| **DevBrowserSettingsState.kt** | ~80 | 持久化状态管理 | @Service + PersistentStateComponent |
| **DevBrowserSettings.kt** | ~20 | 设置数据容器 | Data Class |

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────┐
│        业务层（消费者）                      │
│  DevBrowserPanel / BookmarkManager          │
└────────────────┬────────────────────────────┘
                 │ getInstance(project)
                 ▼
    ┌────────────────────────────────┐
    │  DevBrowserSettingsState       │ @Service
    │  (持久化管理器)                 │
    └─────────┬──────────────────────┘
              │ state: DevBrowserSettings
              ▼
    ┌────────────────────────────────┐
    │  DevBrowserSettings            │ Data Class
    │  (数据容器)                     │
    │  - lastUrl: String             │
    │  - bookmarks: MutableList      │
    └─────────┬──────────────────────┘
              │ XML 序列化/反序列化
              ▼
    ┌────────────────────────────────┐
    │  IntelliJ Platform             │
    │  PersistentStateComponent API  │
    └────────────────────────────────┘
              ↓
    ┌────────────────────────────────┐
    │  .idea/DevBrowserSettings.xml  │
    │  (持久化文件)                   │
    └────────────────────────────────┘
```

---

## 📄 组件详解

### 1. DevBrowserSettingsState (持久化管理器)

**注解配置**:
```kotlin
@Service(Service.Level.PROJECT)
@State(
    name = "DevBrowserSettings",
    storages = [Storage("DevBrowserSettings.xml")]
)
class DevBrowserSettingsState : PersistentStateComponent<DevBrowserSettings>
```

**关键参数**:
- `Service.Level.PROJECT`: 项目级服务（每个项目独立配置）
- `name`: XML 根标签名称
- `storages`: 存储文件路径（相对于 `.idea/` 目录）

**核心 API**:

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getState()` | `DevBrowserSettings` | 获取当前设置状态 |
| `loadState(state)` | `Unit` | 加载设置（反序列化时调用） |
| `getInstance(project)` | `DevBrowserSettingsState` | 获取服务实例 |

**实现逻辑**:
```kotlin
private var myState = DevBrowserSettings()

override fun getState(): DevBrowserSettings {
    return myState
}

override fun loadState(state: DevBrowserSettings) {
    myState = state
}

companion object {
    fun getInstance(project: Project): DevBrowserSettingsState {
        return project.getService(DevBrowserSettingsState::class.java)
    }
}
```

**自动持久化时机**:
- ✅ 项目关闭时
- ✅ IDE 退出时
- ✅ 定期自动保存（IntelliJ 内部机制）

---

### 2. DevBrowserSettings (数据容器)

**属性定义**:
```kotlin
data class DevBrowserSettings(
    var lastUrl: String = "https://www.google.com",
    var bookmarks: MutableList<Bookmark> = mutableListOf()
)
```

**字段说明**:

| 字段 | 类型 | 默认值 | 用途 |
|------|------|--------|------|
| `lastUrl` | `String` | `https://www.google.com` | 上次访问的 URL |
| `bookmarks` | `MutableList<Bookmark>` | 空列表 | 书签列表 |

**XML 序列化示例**:
```xml
<component name="DevBrowserSettings">
  <option name="lastUrl" value="https://github.com" />
  <option name="bookmarks">
    <list>
      <Bookmark>
        <option name="id" value="uuid-1234" />
        <option name="title" value="Google" />
        <option name="url" value="https://www.google.com" />
        <option name="createdAt" value="1697521000000" />
      </Bookmark>
    </list>
  </option>
</component>
```

---

## 🔗 模块间依赖

**被依赖方（消费者）**:
- `../ui/DevBrowserPanel` - 读取 `lastUrl`
- `../bookmark/BookmarkManager` - 管理 `bookmarks` 列表

**依赖的模块**:
- `../bookmark/Bookmark` - 数据模型（需可序列化）
- IntelliJ Platform: `PersistentStateComponent`, `Service`

---

## 📊 数据流

### 读取流程
```
应用启动
    ↓
DevBrowserSettingsState 服务初始化
    ↓
IntelliJ 自动调用 loadState(state)
    ↓
从 .idea/DevBrowserSettings.xml 反序列化
    ↓
myState = DevBrowserSettings(...)
    ↓
业务层调用 getInstance(project).state
    ↓
获取当前设置
```

### 写入流程
```
业务层修改设置
    ↓
settingsState.state.lastUrl = "https://new-url.com"
    ↓
IntelliJ 自动检测状态变化
    ↓
触发序列化（异步）
    ↓
写入 .idea/DevBrowserSettings.xml
```

---

## 🎯 使用示例

### 读取设置
```kotlin
// 在业务层组件中
val settingsState = DevBrowserSettingsState.getInstance(project)
val lastUrl = settingsState.state.lastUrl
```

### 修改设置
```kotlin
// 更新 URL
settingsState.state.lastUrl = "https://example.com"

// 添加书签
settingsState.state.bookmarks.add(
    Bookmark(title = "New Site", url = "https://new.com")
)
```

**注意**: 修改后无需手动保存，IntelliJ 会自动持久化。

---

## ⚠️ 关键注意事项

1. **数据类要求**:
   - 所有属性必须是 `var`（可变）
   - 必须提供无参构造函数（用于反序列化）
   - 嵌套对象（如 `Bookmark`）也必须满足序列化要求

2. **线程安全**:
   - `state` 的读写都在 EDT 线程中进行
   - 无需额外同步机制

3. **默认值**:
   - 首次运行时使用属性默认值
   - 升级插件后，新增字段使用默认值

4. **序列化限制**:
   - 不支持复杂对象（需自定义序列化）
   - 不支持接口和抽象类

---

## 🧪 测试建议

### 单元测试
- [ ] 验证默认值初始化
- [ ] 测试 `getState()` 和 `loadState()` 逻辑
- [ ] 书签列表的增删改查

### 集成测试
- [ ] 修改设置 → 重启 IDE → 验证持久化
- [ ] 多项目并发 → 验证设置隔离
- [ ] 手动编辑 XML → 验证加载正确性

---

## 🔮 扩展建议

### 高优先级
- [ ] 添加设置校验逻辑（防止非法数据）
- [ ] 提供设置重置为默认值的方法
- [ ] 添加设置导入/导出功能

### 中优先级
- [ ] 设置变更监听器（通知其他组件）
- [ ] 支持用户级全局设置（跨项目共享）
- [ ] 设置迁移机制（版本升级时）

### 低优先级
- [ ] 设置加密（敏感信息）
- [ ] 设置版本控制支持（Git 友好）

---

## 📚 参考资源

- [IntelliJ Platform: Persisting State of Components](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html)
- [IntelliJ Platform: Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html)
- [XML Serialization in IntelliJ](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface)

---

## 🐛 常见问题

### Q: 为什么修改了设置但没有保存？
A: 确保修改的是 `state` 对象本身，而非副本：
```kotlin
// ✅ 正确
settingsState.state.lastUrl = "new-url"

// ❌ 错误（修改的是副本）
val settings = settingsState.state.copy()
settings.lastUrl = "new-url"
```

### Q: 如何清除所有设置？
A: 删除 `.idea/DevBrowserSettings.xml` 文件，或重置 state：
```kotlin
settingsState.loadState(DevBrowserSettings())
```

### Q: 为什么 Bookmark 需要无参构造函数？
A: IntelliJ 的 XML 序列化器需要通过反射创建对象实例：
```kotlin
data class Bookmark(...) {
    constructor() : this(...)  // 必需
}
```
