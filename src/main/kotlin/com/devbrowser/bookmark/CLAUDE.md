# 书签模块

> **路径**: [`/CLAUDE.md`](../../../../../CLAUDE.md) → `src/main/kotlin/com/devbrowser/bookmark/`

---

## 📋 模块概述

书签模块提供完整的书签管理功能，包括业务逻辑层（BookmarkManager）、数据模型（Bookmark）和 UI 组件（按钮、弹出菜单、对话框）。

---

## 🗂️ 模块文件清单

| 文件名 | 行数 | 职责 | 类型 |
|--------|------|------|------|
| **BookmarkManager.kt** | 142 | 书签业务逻辑服务 | @Service |
| **Bookmark.kt** | 58 | 书签数据模型 | Data Class |
| **BookmarkActionButton.kt** | ~80 | 添加/删除书签按钮 | UI Component |
| **BookmarkListButton.kt** | ~100 | 书签列表下拉按钮 | UI Component |
| **BookmarkPopupMenu.kt** | ~120 | 书签弹出菜单 | UI Component |
| **EditBookmarkDialog.kt** | ~150 | 书签编辑对话框 | UI Component |

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────┐
│           UI Layer (Toolbar)                │
├─────────────────────────────────────────────┤
│  BookmarkActionButton  │  BookmarkListButton│
│  (添加/删除星标)         │  (书签列表下拉)     │
└──────────┬──────────────┴──────────┬────────┘
           │                         │
           ▼                         ▼
    ┌──────────────┐         ┌──────────────┐
    │ PopupMenu    │         │ EditDialog   │
    │ (右键菜单)   │         │ (编辑对话框)  │
    └──────┬───────┘         └──────┬───────┘
           │                         │
           └──────────┬──────────────┘
                      ▼
            ┌─────────────────────┐
            │  BookmarkManager    │ @Service
            │  (业务逻辑层)        │
            └──────────┬──────────┘
                       ▼
            ┌─────────────────────┐
            │  Bookmark Model     │ Data Class
            │  (数据模型)         │
            └──────────┬──────────┘
                       ▼
            ┌─────────────────────┐
            │ DevBrowserSettings  │
            │ State (持久化)       │
            └─────────────────────┘
```

---

## 📄 组件详解

### 1. BookmarkManager (业务层)

**设计模式**: Service Pattern + Repository Pattern

**职责**:
- 书签的 CRUD 操作
- 数据验证和去重
- 持久化状态管理

**SOLID 原则体现**:
```kotlin
✅ 单一职责 (SRP): 仅负责书签业务逻辑
✅ 依赖倒置 (DIP): 依赖 DevBrowserSettingsState 接口
✅ 开闭原则 (OCP): 可通过继承扩展功能
```

**核心 API**:

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getAllBookmarks()` | `List<Bookmark>` | 获取所有书签（不可变副本） |
| `addBookmark(title, url)` | `Bookmark?` | 添加新书签，失败返回 null |
| `updateBookmark(id, newTitle, newUrl)` | `Boolean` | 更新书签，成功返回 true |
| `deleteBookmark(id)` | `Boolean` | 删除书签 |
| `findBookmarkById(id)` | `Bookmark?` | 根据 ID 查找 |
| `findBookmarkByUrl(url)` | `Bookmark?` | 根据 URL 查找 |
| `isBookmarkExists(url)` | `Boolean` | 检查 URL 是否已存在 |

**数据验证逻辑** (34行):
```kotlin
addBookmark() {
    1. 验证 Bookmark.isValid()
    2. 检查 URL 是否重复
    3. 添加到列表
}
```

**去重策略**:
- 以 `url` 为唯一标识符
- 更新时检查新 URL 是否与其他书签冲突

---

### 2. Bookmark (数据模型)

**属性**:
```kotlin
id: String              // UUID 唯一标识
title: String           // 书签标题
url: String             // 书签 URL
createdAt: Long         // 创建时间戳（毫秒）
```

**验证规则** (32行):
```kotlin
isValid(): Boolean {
    title.isNotBlank() &&
    url.isNotBlank() &&
    (url.startsWith("http://") || url.startsWith("https://"))
}
```

**XML 序列化支持**:
- 提供无参构造函数（IntelliJ 持久化要求）
- 使用 `data class` 自动生成序列化方法

---

### 3. UI 组件

#### BookmarkActionButton (星标按钮)

**功能**:
- 根据当前 URL 显示"已收藏"或"未收藏"状态
- 点击添加/删除书签

**状态管理**:
```kotlin
updateButtonState() {
    根据当前 URL 查询是否已收藏
    → 更新图标（星标填充/空心）
    → 更新 tooltip
}
```

**事件回调**:
```kotlin
onBookmarkChanged: (() -> Unit)?
// 书签变化时通知其他组件更新
```

#### BookmarkListButton (列表按钮)

**功能**:
- 显示书签数量徽章
- 点击弹出书签列表菜单

**菜单集成**:
```kotlin
点击 → 创建 BookmarkPopupMenu
     → 定位到按钮下方
     → 显示弹出菜单
```

#### BookmarkPopupMenu (弹出菜单)

**功能**:
- 显示所有书签列表
- 提供"编辑"和"删除"右键菜单
- 点击书签跳转 URL

**菜单结构**:
```
┌─────────────────────────┐
│ 📄 Google (https://...) │ → 左键：跳转
│ 📄 GitHub (https://...) │ → 右键：编辑/删除
│ 📄 MDN (https://...)    │
└─────────────────────────┘
```

#### EditBookmarkDialog (编辑对话框)

**功能**:
- 编辑书签标题和 URL
- 数据验证和错误提示

**验证流程**:
```
用户输入 → 验证格式 → 调用 BookmarkManager.updateBookmark()
                     → 成功：关闭对话框
                     → 失败：显示错误消息
```

---

## 🔗 模块间依赖

**依赖的模块**:
- `../settings/DevBrowserSettingsState` - 持久化存储
- `../settings/DevBrowserSettings` - 数据容器

**被依赖方**:
- `../ui/BrowserToolbar` - 集成书签按钮

---

## 📊 数据流

```
用户点击添加书签
    ↓
BookmarkActionButton.onClick()
    ↓
获取当前 URL 和页面标题
    ↓
BookmarkManager.addBookmark(title, url)
    ↓
验证数据 + 检查重复
    ↓
添加到 settingsState.state.bookmarks
    ↓
触发 onBookmarkChanged 回调
    ↓
BookmarkListButton.updateButtonState()
```

---

## 🎯 设计原则应用

### KISS (简单至上)
- 数据模型极简，仅 4 个字段
- API 语义清晰，无过度抽象

### YAGNI (精益求精)
- 暂不支持书签分组、标签等高级功能
- 仅实现当前必需的增删改查

### DRY (避免重复)
- 验证逻辑统一在 `Bookmark.isValid()` 和 `BookmarkManager`
- UI 组件共享同一 `BookmarkManager` 实例

---

## ⚠️ 关键注意事项

1. **线程安全**: 所有 UI 操作必须在 EDT 线程
2. **状态同步**: 书签变化后必须调用回调通知其他组件
3. **URL 去重**: 以 `url` 为唯一标识，不允许重复
4. **数据持久化**: 依赖 IntelliJ 的 `PersistentStateComponent`，自动保存

---

## 🧪 测试建议

### 单元测试
- [ ] `BookmarkManager.addBookmark()` - 验证去重逻辑
- [ ] `Bookmark.isValid()` - 各种非法数据测试
- [ ] `BookmarkManager.updateBookmark()` - URL 冲突测试

### 集成测试
- [ ] 添加书签 → 刷新页面 → 验证星标状态
- [ ] 书签列表 → 编辑 → 验证同步更新
- [ ] 删除书签 → 验证列表和按钮更新

---

## 🔮 扩展建议

### 高优先级
- [ ] 书签导入/导出（JSON 格式）
- [ ] 书签搜索和过滤
- [ ] 书签排序（按标题/时间）

### 低优先级
- [ ] 书签分组/文件夹
- [ ] 书签图标（Favicon）
- [ ] 书签标签系统
