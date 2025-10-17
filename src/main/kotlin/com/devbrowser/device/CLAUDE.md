# 设备模块

> **路径**: [`/CLAUDE.md`](../../../../../CLAUDE.md) → `src/main/kotlin/com/devbrowser/device/`

---

## 📋 模块概述

设备模块实现 PC 和移动端浏览器环境切换，通过 User-Agent 修改和视口适配模拟移动设备浏览体验。

---

## 🗂️ 模块文件清单

| 文件名 | 行数 | 职责 | 类型 |
|--------|------|------|------|
| **DeviceModeController.kt** | 118 | 设备模式协调器 | Controller |
| **UserAgentHandler.kt** | ~90 | User-Agent 处理器 | CefResourceRequestHandler |
| **MobileViewportAdapter.kt** | ~120 | 移动视口适配器 | Adapter |
| **MobileDevice.kt** | 57 | 移动设备配置 | Data Class |
| **DeviceModeButton.kt** | ~80 | 设备切换按钮 | UI Component |

---

## 🏗️ 架构设计

```
┌──────────────────────────────────────────────────┐
│            DeviceModeButton (UI)                 │
│            (PC/移动切换按钮)                      │
└────────────────────┬─────────────────────────────┘
                     │ onModeChanged 回调
                     ▼
        ┌────────────────────────────┐
        │  DeviceModeController      │ 协调层
        │  (统一管理设备模式切换)     │
        └─────────┬──────────┬───────┘
                  │          │
         ┌────────┘          └────────┐
         ▼                            ▼
┌─────────────────┐        ┌──────────────────────┐
│ UserAgentHandler│        │ MobileViewportAdapter│
│ (修改请求头)    │        │ (调整浏览器视口)      │
└────────┬────────┘        └──────────┬───────────┘
         │                            │
         └──────────┬─────────────────┘
                    ▼
           ┌─────────────────┐
           │  MobileDevice   │ 数据模型
           │  (设备配置)      │
           └─────────────────┘
                    │
                    ▼
        ┌───────────────────────────┐
        │  JCEF Browser (CEF)       │
        │  - User-Agent 生效         │
        │  - Viewport 生效           │
        └───────────────────────────┘
```

---

## 📄 组件详解

### 1. DeviceModeController (协调器)

**设计模式**: Facade Pattern

**职责**:
- 统一协调 User-Agent 和视口适配
- 管理当前设备模式状态
- 提供简单的 API 给 UI 层调用

**核心 API**:

| 方法 | 参数 | 说明 |
|------|------|------|
| `switchToMobileMode(device)` | `MobileDevice` | 切换到移动端（默认 iPhone 14） |
| `switchToPCMode()` | - | 恢复 PC 模式 |
| `toggleMode()` | - | 在 PC/移动间切换 |
| `isMobileMode()` | - | 检查是否为移动模式 |
| `getCurrentDevice()` | - | 获取当前设备配置 |

**初始化流程** (23行):
```kotlin
init {
    注册 UserAgentHandler 到浏览器
    → 创建 CefRequestHandler
    → 返回 UserAgentHandler 作为 ResourceRequestHandler
    → browser.jbCefClient.addRequestHandler()
}
```

**模式切换流程** (53行):
```kotlin
switchToMobileMode(device) {
    1. 保存设备配置到 currentDevice
    2. userAgentHandler.setMobileDevice(device)
    3. viewportAdapter.setMobileDevice(device)
    4. browser.cefBrowser.reload() // 刷新应用新 UA
}
```

---

### 2. UserAgentHandler (User-Agent 处理器)

**设计模式**: Strategy Pattern

**继承关系**:
```kotlin
extends CefResourceRequestHandlerAdapter
implements CefResourceRequestHandler
```

**职责**:
- 拦截所有 HTTP 请求
- 修改请求头中的 User-Agent 字段

**核心逻辑**:
```kotlin
override fun onBeforeResourceLoad(
    browser: CefBrowser?,
    frame: CefFrame?,
    request: CefRequest?
): Boolean {
    if (currentDevice != null) {
        request.setHeaderByName("User-Agent", currentDevice.userAgent, true)
    }
    return false // 继续加载
}
```

**User-Agent 切换**:
```kotlin
setMobileDevice(device: MobileDevice?) {
    currentDevice = device
    // 立即生效于下次请求
}
```

---

### 3. MobileViewportAdapter (视口适配器)

**设计模式**: Adapter Pattern

**职责**:
- 通过 JavaScript 注入修改浏览器视口
- 设置 `viewport` meta 标签
- 调整 `window.innerWidth/innerHeight`

**核心实现**:
```kotlin
setMobileDevice(device: MobileDevice?) {
    if (device != null) {
        injectMobileViewport(device)
    } else {
        removeMobileViewport()
    }
}

private fun injectMobileViewport(device: MobileDevice) {
    val js = """
        // 设置 viewport meta
        var meta = document.querySelector('meta[name=viewport]');
        if (!meta) {
            meta = document.createElement('meta');
            meta.name = 'viewport';
            document.head.appendChild(meta);
        }
        meta.content = 'width=${device.width}, initial-scale=1.0';

        // 模拟设备像素比
        Object.defineProperty(window, 'devicePixelRatio', {
            get: () => ${device.devicePixelRatio}
        });
    """
    browser.getCefBrowser().executeJavaScript(js, url, 0)
}
```

---

### 4. MobileDevice (设备配置)

**属性**:
```kotlin
name: String                // 设备名称（如 "iPhone 14"）
width: Int                  // 视口宽度（像素）
height: Int                 // 视口高度（像素）
devicePixelRatio: Double    // 设备像素比（DPR）
userAgent: String           // User-Agent 字符串
```

**预设设备**:

**iPhone 14** (27行):
```kotlin
width = 390
height = 844
devicePixelRatio = 3.0
userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) ..."
```

**验证规则** (46行):
```kotlin
isValid(): Boolean {
    width > 0 &&
    height > 0 &&
    devicePixelRatio > 0 &&
    userAgent.isNotBlank()
}
```

---

### 5. DeviceModeButton (切换按钮)

**功能**:
- 显示当前模式（PC/移动）
- 点击切换模式
- 更新图标和 tooltip

**事件回调**:
```kotlin
onModeChanged: ((isMobileMode: Boolean) -> Unit)?
// 模式切换时通知 Controller
```

**状态管理**:
```kotlin
updateButtonState(isMobileMode: Boolean) {
    icon = if (isMobileMode) MobileIcon else PCIcon
    toolTipText = "当前：${mode}（点击切换）"
}
```

---

## 🔗 模块间依赖

**依赖的模块**:
- IntelliJ Platform: `JBCefBrowser`, `JBCefClient`
- CEF: `CefBrowser`, `CefRequest`, `CefResourceRequestHandler`

**被依赖方**:
- `../ui/BrowserToolbar` - 集成设备切换按钮

---

## 📊 数据流

```
用户点击设备切换按钮
    ↓
DeviceModeButton.onClick()
    ↓
触发 onModeChanged(isMobileMode = true)
    ↓
DeviceModeController.switchToMobileMode()
    ↓
    ├─→ UserAgentHandler.setMobileDevice(IPHONE_14)
    │       ↓
    │   下次请求时修改 User-Agent 头
    │
    └─→ MobileViewportAdapter.setMobileDevice(IPHONE_14)
            ↓
        注入 JavaScript 修改视口和 DPR
    ↓
browser.cefBrowser.reload()
    ↓
页面重新加载，应用新的 UA 和视口
```

---

## 🎯 技术实现细节

### User-Agent 修改原理

**CEF 请求拦截**:
```
HTTP 请求发起
    ↓
CefRequestHandler.getResourceRequestHandler()
    ↓
返回 UserAgentHandler
    ↓
UserAgentHandler.onBeforeResourceLoad()
    ↓
request.setHeaderByName("User-Agent", mobileUA, true)
    ↓
继续请求（带有新的 UA）
```

### 视口适配原理

**JavaScript 注入时机**:
- 页面加载完成后（`CefLoadHandler.onLoadEnd`）
- 立即执行 `executeJavaScript()`

**模拟的 Web API**:
```javascript
// Viewport Meta
<meta name="viewport" content="width=390, initial-scale=1.0">

// Device Pixel Ratio
window.devicePixelRatio = 3.0
```

---

## ⚠️ 关键注意事项

1. **User-Agent 生效时机**: 需要刷新页面才能应用新 UA
2. **视口注入时机**: 必须在页面加载完成后执行
3. **设备配置验证**: 使用前必须调用 `isValid()` 验证
4. **线程安全**: CEF 回调在 CEF 线程，UI 更新需切换到 EDT

---

## 🧪 测试建议

### 单元测试
- [ ] `MobileDevice.isValid()` - 非法配置验证
- [ ] `DeviceModeController.toggleMode()` - 状态切换逻辑

### 集成测试
- [ ] 切换到移动模式 → 刷新页面 → 验证 User-Agent
- [ ] 检查 `window.devicePixelRatio` 是否正确
- [ ] 验证视口宽度是否符合设备配置

### 手动测试
```javascript
// 在浏览器控制台执行
console.log(navigator.userAgent);       // 检查 User-Agent
console.log(window.devicePixelRatio);   // 检查 DPR
console.log(window.innerWidth);         // 检查视口宽度
```

---

## 🔮 扩展建议

### 高优先级
- [ ] 添加更多设备预设（iPad, Android, iPhone 15 等）
- [ ] 支持自定义设备配置（用户输入参数）
- [ ] 持久化当前设备模式选择

### 中优先级
- [ ] 设备旋转模拟（横屏/竖屏切换）
- [ ] 网络节流模拟（3G/4G/5G）
- [ ] 触摸事件模拟（模拟手指触摸）

### 低优先级
- [ ] GPS 位置模拟
- [ ] 传感器模拟（陀螺仪、加速度计）
- [ ] 设备方向 API 模拟

---

## 📚 参考资源

- [User-Agent Strings](https://www.useragentstring.com/)
- [Viewport Meta Tag](https://developer.mozilla.org/en-US/docs/Web/HTML/Viewport_meta_tag)
- [CEF Request Handling](https://bitbucket.org/chromiumembedded/cef/wiki/GeneralUsage.md#markdown-header-request-handling)
