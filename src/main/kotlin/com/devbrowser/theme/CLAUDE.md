# 主题模块

> **路径**: [`/CLAUDE.md`](../../../../../CLAUDE.md) → `src/main/kotlin/com/devbrowser/theme/`

---

## 📋 模块概述

主题模块负责将 IntelliJ IDEA 的 Darcula 深色主题适配到嵌入式浏览器，通过 CSS 注入实现网页与 IDE 主题的视觉统一。

---

## 🗂️ 模块文件清单

| 文件名 | 行数 | 职责 | 类型 |
|--------|------|------|------|
| **DarculaThemeAdapter.kt** | ~150 | Darcula 主题适配器 | Adapter |

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────┐
│     BrowserToolbar (UI)             │
│     themeToggleButton               │
└──────────────┬──────────────────────┘
               │ toggleTheme()
               ▼
    ┌──────────────────────────┐
    │  DarculaThemeAdapter     │
    │  (CSS 注入管理)           │
    └───────┬──────────────────┘
            │
            ├─→ enableTheme()
            │       ↓
            │   注入 Darcula CSS
            │
            └─→ disableTheme()
                    ↓
                移除注入的 CSS
            ↓
    ┌──────────────────────────┐
    │  JBCefBrowser (CEF)      │
    │  executeJavaScript()     │
    └──────────────────────────┘
            ↓
    ┌──────────────────────────┐
    │  Web Page DOM            │
    │  <style id="darcula">    │
    └──────────────────────────┘
```

---

## 📄 组件详解

### DarculaThemeAdapter

**设计模式**: Adapter Pattern

**职责**:
- 将 IDE Darcula 主题适配到 Web 环境
- 通过 JavaScript 注入/移除 CSS 样式
- 监听页面加载事件，自动应用主题

**核心属性**:
```kotlin
private val browser: JBCefBrowser    // JCEF 浏览器实例
private var themeEnabled: Boolean    // 主题启用状态
private val THEME_STYLE_ID = "darcula-theme-adapter"  // CSS <style> 标签 ID
```

**核心 API**:

| 方法 | 说明 |
|------|------|
| `enableTheme()` | 启用 Darcula 主题 |
| `disableTheme()` | 禁用 Darcula 主题 |
| `isThemeEnabled()` | 检查主题是否启用 |
| `reapplyTheme()` | 重新应用主题（用于页面导航） |

**初始化流程**:
```kotlin
init {
    setupLoadHandler()  // 监听页面加载
    if (themeEnabled) {
        // 初始状态已启用，等待页面加载后注入
    }
}
```

---

## 🎨 CSS 注入实现

### 核心 JavaScript 代码

```javascript
(function() {
    // 移除旧样式（如果存在）
    var oldStyle = document.getElementById('darcula-theme-adapter');
    if (oldStyle) oldStyle.remove();

    // 创建新样式标签
    var style = document.createElement('style');
    style.id = 'darcula-theme-adapter';
    style.textContent = `
        /* Darcula 主题 CSS */
        body {
            background-color: #2B2B2B !important;
            color: #A9B7C6 !important;
        }

        /* 输入框样式 */
        input, textarea, select {
            background-color: #313335 !important;
            color: #A9B7C6 !important;
            border-color: #555555 !important;
        }

        /* 链接样式 */
        a {
            color: #589DF6 !important;
        }

        /* 代码块样式 */
        pre, code {
            background-color: #313335 !important;
            color: #A9B7C6 !important;
        }
    `;

    // 添加到 <head>
    document.head.appendChild(style);
})();
```

### 注入时机

**监听页面加载完成**:
```kotlin
private fun setupLoadHandler() {
    browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
        override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
            if (frame?.isMain == true && themeEnabled) {
                SwingUtilities.invokeLater {
                    injectDarculaCSS()
                }
            }
        }
    }, browser.cefBrowser)
}
```

**执行时机**:
- ✅ 主框架加载完成后（`frame.isMain == true`）
- ✅ 主题已启用时（`themeEnabled == true`）
- ✅ Swing EDT 线程中执行（`SwingUtilities.invokeLater`）

---

## 🎯 Darcula 配色方案

### 核心颜色定义

| 元素 | 颜色代码 | 说明 |
|------|----------|------|
| **背景色** | `#2B2B2B` | 主背景（与 IDE 编辑器一致） |
| **文本色** | `#A9B7C6` | 主文本颜色 |
| **输入框背景** | `#313335` | 输入框、文本域背景 |
| **边框色** | `#555555` | 边框和分隔线 |
| **链接色** | `#589DF6` | 超链接蓝色 |
| **代码块背景** | `#313335` | 代码块背景 |

### CSS 优先级策略

使用 `!important` 确保样式覆盖网页原有样式：
```css
background-color: #2B2B2B !important;
```

---

## 🔗 模块间依赖

**依赖的模块**:
- IntelliJ Platform: `JBCefBrowser`, `JBCefClient`
- CEF: `CefLoadHandler`, `CefBrowser`
- Swing: `SwingUtilities`

**被依赖方**:
- `../ui/DevBrowserPanel` - 创建主题适配器实例
- `../ui/BrowserToolbar` - 通过 `setThemeAdapter()` 关联

---

## 📊 数据流

```
用户点击主题切换按钮
    ↓
BrowserToolbar.toggleTheme()
    ↓
DarculaThemeAdapter.enableTheme()
    ↓
设置 themeEnabled = true
    ↓
调用 injectDarculaCSS()
    ↓
browser.executeJavaScript(cssInjectionScript)
    ↓
    ├─→ 创建 <style id="darcula-theme-adapter">
    │
    └─→ 添加到 document.head
    ↓
页面应用 Darcula 主题样式
```

---

## ⚠️ 关键注意事项

1. **CSS 覆盖范围**: `!important` 可能无法覆盖内联样式（`style="..."`）
2. **页面导航**: 每次页面跳转需重新注入（通过 `onLoadEnd` 自动处理）
3. **JavaScript 执行时机**: 必须在主框架加载完成后（`frame.isMain`）
4. **样式 ID 唯一性**: 使用固定 ID 避免重复注入

---

## 🧪 测试建议

### 单元测试
- [ ] `enableTheme()` → 验证 `themeEnabled == true`
- [ ] `disableTheme()` → 验证移除 CSS 的 JavaScript 执行

### 集成测试
- [ ] 启用主题 → 刷新页面 → 验证样式保持
- [ ] 页面导航 → 验证自动重新注入
- [ ] 禁用主题 → 验证恢复原始样式

### 手动测试
```javascript
// 在浏览器控制台执行
var style = document.getElementById('darcula-theme-adapter');
console.log(style ? '✅ 主题已应用' : '❌ 主题未应用');
console.log(getComputedStyle(document.body).backgroundColor); // 应为 #2B2B2B
```

---

## 🔮 扩展建议

### 高优先级
- [ ] 支持 IntelliJ Light 主题（自动检测当前 IDE 主题）
- [ ] 提供主题强度调节（部分覆盖 vs 完全覆盖）
- [ ] 用户自定义 CSS 注入功能

### 中优先级
- [ ] 智能主题检测（分析网页主色调，决定是否应用）
- [ ] 针对特定网站的 CSS 规则库（如 GitHub、MDN）
- [ ] 主题切换动画效果

### 低优先级
- [ ] 导出/导入自定义主题配置
- [ ] 社区主题市场（分享 CSS 规则）

---

## 🐛 已知限制

1. **无法覆盖的样式**:
   - 内联样式 `style="color: red !important"`
   - Shadow DOM 内的样式

2. **兼容性问题**:
   - 某些网站使用 CSS-in-JS，可能无法覆盖
   - 动态加载的内容需重新应用主题

3. **性能影响**:
   - 每次页面加载都执行 JavaScript（微小开销）

---

## 📚 参考资源

- [IntelliJ Darcula 配色](https://plugins.jetbrains.com/docs/intellij/themes-getting-started.html)
- [CSS !important 优先级](https://developer.mozilla.org/en-US/docs/Web/CSS/Specificity)
- [CEF JavaScript Execution](https://bitbucket.org/chromiumembedded/cef/wiki/JavaScriptIntegration.md)
