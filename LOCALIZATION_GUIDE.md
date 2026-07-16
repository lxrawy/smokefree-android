# SmokeFree — 全球化适配指南

> 本文档列出应用从「仅中文」到「全球多语言可用」需要做的全部改动。

---

## 一、已完成

| 项目 | 状态 |
|------|------|
| 英文 strings.xml | ✅ 已创建 `values-en/strings.xml` |
| 双语隐私政策 | ✅ 已更新 `privacy_policy.html`（英文+中文切换） |
| 上架指南 | ✅ 已重写为 Google Play + Apple App Store 版 |

---

## 二、代码层面必须改的

### 2.1 包名 `com.example.smokefree` → 真实域名

**Google Play 直接拒绝 `com.example.*` 包名。**

```kotlin
// app/build.gradle.kts
namespace = "com.smokefree.app"        // 改为你的域名
applicationId = "com.smokefree.app"
```

```xml
<!-- AndroidManifest.xml -->
<manifest package="com.smokefree.app">
```

```bash
# 重命名源码目录
mv app/src/main/java/com/example/smokefree app/src/main/java/com/smokefree/app
```

所有 Kotlin 文件的 `package com.example.smokefree` → `package com.smokefree.app`

> 推荐用 Android Studio: 右键包名 → Refactor → Rename

### 2.2 登录方式适配全球

**当前问题：**
- 手机号登录：只接受 11 位中国手机号格式（`请输入11位手机号`）
- 微信登录：海外用户几乎不使用

**推荐方案（按工作量从小到大）：**

| 方案 | 说明 | 优缺点 |
|------|------|--------|
| A. 去掉登录 | 直接进主界面，数据全本地 | 最快上线；但用户无法多设备同步（当前也没有同步） |
| B. Email 登录 | 邮箱+密码，本地验证 | 全球通用；但无服务器无法真正验证 |
| C. Google + Apple 登录 | 官方 SDK | 最佳体验；但需集成 SDK，且 Apple 强制要求同时提供 Apple Sign-In |

> **建议：方案 A**。当前应用数据全本地存储，登录只是模拟，去掉登录页直接进入主界面最务实。

如果保留登录并使用第三方登录：
- **Apple 强制要求**：提供任何第三方登录（Google/Facebook/微信），必须同时提供 Sign in with Apple
- **Google Play**：建议提供 Google Sign-In

### 2.3 移除不必要的 INTERNET 权限

当前 `AndroidManifest.xml` 声明了 `INTERNET` 权限，但应用声称不联网。

```xml
<!-- 如果确实不联网，建议移除这行 -->
<uses-permission android:name="android.permission.INTERNET" />
```

> Google Play 的数据安全表需要与实际权限一致。保留 INTERNET 但声明"不收集数据"可能触发额外审查。

### 2.4 添加"删除账户"功能（Apple 强制要求）

Apple App Store 审核指南 5.1.1 要求：如果应用提供账户创建，必须提供**完整的账户删除**功能（不只是退出登录）。

在「我的」或「账户信息」页面添加：
```
[删除账户] → 确认弹窗 → 清除所有本地数据 → 回到登录/首页
```

### 2.5 targetSdk 确认

```kotlin
// build.gradle.kts
targetSdk = 34  // 2025年 Google Play 要求 ≥ 34
```

---

## 三、应用内文案国际化

### 3.1 硬编码中文字符串

检查所有 Kotlin 文件和 XML 布局中是否有**硬编码的中文字符串**（未通过 `@string/` 引用）。

常见位置：
- `Toast.makeText(context, "中文文字", ...)` → 改为 `getString(R.string.xxx)`
- XML 布局中 `android:text="中文"` → 改为 `android:text="@string/xxx"`
- `AlertDialog.Builder().setTitle("中文")` → 改为 `getString(R.string.xxx)`

### 3.2 货币符号

当前使用 `¥`（人民币符号）。全球版建议：
- 根据用户系统区域设置自动选择货币符号
- 或在设置中让用户选择货币

```kotlin
val currencySymbol = if (Locale.getDefault().country == "CN") "¥" else "$"
```

### 3.3 日期格式

当前使用 `yyyy-MM-dd`（中文习惯）。英文环境建议：
```kotlin
val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
// 美国: Jul 16, 2026
// 中国: 2026年7月16日
// 日本: 2026/07/16
```

### 3.4 数字格式

大数字使用千分位分隔符：
```kotlin
val formatted = String.format(Locale.getDefault(), "%,d", amount)
// 英文: 1,234
// 中文: 1,234
```

---

## 四、多语言扩展路线图

### 第一阶段（首发上线）
- ✅ English (en) — `values-en/strings.xml`
- ✅ Chinese (zh) — `values/strings.xml`（已有）
- 建议补充：商店详情翻译为英文+中文

### 第二阶段（上线后 1-2 月）
- 日语 (ja) — `values-ja/strings.xml`
- 韩语 (ko) — `values-ko/strings.xml`
- 西班牙语 (es) — `values-es/strings.xml`

### 第三阶段（按下载量决定）
- 葡萄牙语 (pt) — 巴西市场
- 法语 (fr)
- 德语 (de)
- 俄语 (ru)
- 阿拉伯语 (ar) — 需要 RTL 布局适配

### 添加新语言步骤

1. 创建 `res/values-{lang}/strings.xml`
2. 翻译所有字符串（保持 key 不变）
3. 在 Google Play Console / App Store Connect 添加对应语言的商店详情
4. 提供对应语言的截图（可选，但推荐）

---

## 五、商店详情本地化

### Google Play
- Play Console → Store presence → Main store listing
- 添加语言 → 每种语言独立填写：名称、简短描述、完整描述、截图

### Apple App Store
- App Store Connect → App Information → Localizations
- 添加语言 → 每种语言独立填写：名称、副标题、描述、关键词、截图

**优先翻译的商店详情文案：**

```
[App Name]
EN: SmokeFree
ZH: 戒烟助手
JA: 禁煙ガイド
KO: 금연도우미
ES: SmokeFree

[Short Description]
EN: Quit smoking, one day at a time.
ZH: 科学戒烟，每一天都更有活力。
JA: 一步一步、禁煙を成功させよう。
KO: 하루하루, 금연에 도전하세요.
ES: Deja de fumar, un día a la vez.

[Keywords]
EN: quit smoking, smoke free, stop smoking, quit tracker, health
ZH: 戒烟,控烟,健康,省钱,习惯养成
JA: 禁煙,タバコ,健康,節約
KO: 금연,담배,건강,절약
ES: dejar de fumar, sin humo, salud
```

---

## 六、RTL（从右到左）语言适配

如果将来支持阿拉伯语/希伯来语：
1. `AndroidManifest.xml` 中 `android:supportsRtl="true"`（已有 ✅）
2. 布局中使用 `start/end` 而非 `left/right`
3. drawable 方向性资源提供 mirrored 版本
4. 测试 RTL 布局：开发者选项 → 强制 RTL 布局方向

> 首发不建议支持 RTL 语言，工作量大且优先级低。

---

## 七、检查清单

### 代码
- [ ] 包名改为真实域名
- [ ] 登录方式适配全球用户
- [ ] 移除不必要的 INTERNET 权限
- [ ] 添加"删除账户"功能
- [ ] targetSdk ≥ 34
- [ ] 所有硬编码中文移至 strings.xml
- [ ] 货币符号根据区域自适应
- [ ] 日期格式根据区域自适应

### 资源
- [ ] values-en/strings.xml 已创建
- [ ] 应用图标已适配（512×512 for GP, 1024×1024 for Apple）
- [ ] 截图已准备英文版

### 商店
- [ ] Google Play 商店详情（英文）
- [ ] Apple App Store 商店详情（英文）
- [ ] 隐私政策 URL 可公开访问
- [ ] 数据安全表已填写（Google Play）
- [ ] 隐私标签已填写（Apple）
- [ ] 内容分级问卷已完成（两平台）
