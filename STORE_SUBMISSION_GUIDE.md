# SmokeFree — Google Play & Apple App Store 上架指南

> 目标市场：全球（首发英文为主，中文为辅）
> 不上架国内商店（小米/华为/应用宝/OPPO/vivo），因此**不需要软件著作权（软著）**。

---

## 〇、当前状态与必须解决的阻断项

### 阻断项 1：包名 `com.example.smokefree` 必须改！

Google Play **拒绝**任何以 `com.example` 开头的包名。必须改为你已购买域名对应的反向域名。

```
# 假设你购买的域名是 smokefree.app
# 则包名应为：
com.smokefree.app
```

需要同步修改的文件：
| 文件 | 修改内容 |
|------|----------|
| `app/build.gradle.kts` | `applicationId` 和 `namespace` |
| `app/src/main/AndroidManifest.xml` | `package` 属性 |
| 所有 Kotlin 文件的 `package` 声明 | 全局替换 |
| `app/src/main/java/com/example/smokefree/` | 文件夹重命名为 `com/smokefree/app/` |

> 推荐用 Android Studio 的 `Build → Rename Package` 功能自动完成。

### 阻断项 2：没有 iOS 工程

当前只有 Android 项目。要上架 Apple App Store 必须有 iOS 应用（Swift/SwiftUI 或 Flutter/React Native 跨平台）。

**选项：**
- A. 用 Flutter / React Native 重写为跨平台（一套代码两端发布）
- B. 单独开发 iOS 原生版本（Swift + SwiftUI）
- C. 先只上 Google Play，iOS 以后再说

### 阻断项 3：登录方式不适合全球用户

当前登录逻辑：
- 手机号登录：只支持 11 位中国手机号格式
- 微信登录：海外用户几乎无人使用

**全球版建议方案（代码层面需要改）：**
| 方案 | 说明 | 工作量 |
|------|------|--------|
| A. 去掉登录，纯本地使用 | 数据全本地存储，无需账号 | 最小 |
| B. Email 登录 | 简单邮箱+密码，本地验证 | 中 |
| C. Google Sign-In + Apple Sign-In | 官方登录 SDK | 较大 |

> 当前应用数据全是本地存储（SharedPreferences），登录只是模拟，**方案 A 最务实**：去掉登录页，直接进入主界面。Apple 强制要求：如果提供第三方登录，必须同时提供 Apple Sign-In。

### 阻断项 4：strings.xml 只有中文

全球发布至少需要英文。详见 `LOCALIZATION_GUIDE.md`。

---

## 一、开发者账号

### Google Play 开发者账号

| 项目 | 详情 |
|------|------|
| 费用 | $25（一次性，终身有效） |
| 注册地址 | https://play.google.com/console |
| 所需材料 | Google 账号 + 信用卡/借记卡 + 手机号验证 |
| 审核时间 | 1-3 天（身份验证） |
| 注意 | 个人开发者即可；新账号上架首几个应用会有额外审核（可能 3-7 天） |

### Apple Developer 账号

| 项目 | 详情 |
|------|------|
| 费用 | $99/年 |
| 注册地址 | https://developer.apple.com |
| 所需材料 | Apple ID + 双重认证 + 信用卡 |
| 审核时间 | 1-2 天 |
| 注意 | 个人开发者即可；如果上架付费应用/内购需要 DUNS 编号（免费应用不需要） |

---

## 二、应用包文件准备

### Android（Google Play）

```
构建命令：
cd android-smokefree
./gradlew bundleRelease

产物路径：
app/build/outputs/bundle/release/app-release.aab
```

Google Play **只接受 AAB 格式**（不接受 APK）。AAB 由 Google Play 自动按设备拆分分发。

签名说明：
- Google Play 使用 **Play App Signing**：你上传密钥签名的 AAB，Google 用自己的最终签名密钥分发给用户
- 你需要保留上传密钥（`smokefree-release.keystore`），Google 保留最终签名密钥
- 配置路径：Play Console → 选择应用 → Setup → App Integrity → Enroll

### iOS（Apple App Store）

```
构建方式：
Xcode → Product → Archive → Distribute App → App Store Connect
```

需要：
- Distribution Certificate（发布证书）
- Provisioning Profile（发布描述文件）
- App ID（在 Apple Developer Portal 注册）

---

## 三、隐私政策（国际合规版）

### 必须满足的法律框架

| 法律 | 适用地区 | 核心要求 |
|------|----------|----------|
| **GDPR** | 欧盟/英国 | 法律依据、用户权利清单、数据保留期限、DPO 联系方式 |
| **CCPA/CPRA** | 美国加州 | 消费者权利、不出售数据声明、 opt-out 机制 |
| **COPPA** | 美国 | 13 岁以下儿童数据限制 |
| **PIPL** | 中国 | 即使不上架国内商店，中国用户通过海外商店下载也适用 |
| **Apple ATT** | 全球 iOS | 如果追踪用户跨应用行为，需弹窗授权（本应用不追踪，无需弹窗） |

### 隐私政策必须包含的内容

已为你准备好双语版隐私政策文件 `privacy_policy.html`，包含以下完整章节：

1. **生效日期与版本号**
2. **收集的数据类型**（明确分类：个人数据 vs 非个人数据）
3. **数据使用目的**（逐条列出）
4. **法律依据**（GDPR 要求：同意/合同/合法利益）
5. **数据存储与安全**（本地存储、不联网传输）
6. **数据保留期限**（明确说明：用户主动删除前一直保留）
7. **第三方服务**（明确声明：无广告、无分析、无 SDK）
8. **用户权利**（GDPR 全套：访问/更正/删除/可携带性/反对/限制处理）
9. **儿童隐私**（COPPA + GDPR-K：不面向 13 岁以下）
10. **国际数据传输**（说明数据不跨境传输，纯本地）
11. **政策更新机制**
12. **联系方式**（必须有邮箱地址 — 见下方）

### 隐私政策托管

将 `privacy_policy.html` 上传到你的域名，例如：
```
https://your-domain.com/privacy-policy
```

> 两个商店都需要填写一个可公开访问的隐私政策 URL。

---

## 四、Google Play 上架流程

### 第 1 步：创建应用

Play Console → 「创建应用」→ 填写：

| 字段 | 填写内容 |
|------|----------|
| App name | SmokeFree |
| Default language | English (United States) |
| App type | App |
| Free or paid | Free |

### 第 2 步：填写商店详情（Store Listing）

| 字段 | 要求 |
|------|------|
| App name | SmokeFree（最多 30 字符） |
| Short description | Quit smoking, one day at a time.（最多 80 字符） |
| Full description | 最多 4000 字符，英文为主 |
| App icon | 512×512 px PNG（32-bit PNG，alpha 通道） |
| Feature graphic | 1024×500 px PNG/JPG（**必须有，首页横幅**） |
| Phone screenshots | 至少 2 张，推荐 4-8 张；尺寸 1080×1920 或 1440×2560 |
| App category | Health & Fitness |
| Tags | Health, Habit Tracker |
| Privacy Policy URL | 填入你的域名隐私政策链接 |

**多语言商店详情**（强烈建议）：
- Settings → Advanced Settings → Languages → 添加语言
- 建议首发：English + Simplified Chinese + Japanese + Korean + Spanish
- 每种语言独立填写名称、描述、截图

### 第 3 步：数据安全表（Data Safety Form）

Google Play 强制要求。Play Console → App Content → Data Safety：

| 问题 | 回答 |
|------|------|
| Does your app collect or share user data? | Yes（用户输入的吸烟记录） |
| Data collected | Personal info → Other personal info（吸烟习惯数据） |
| Is data encrypted in transit? | N/A（不传输数据） |
| Can users request data deletion? | Yes（应用内可清除所有数据 + 卸载即可删除） |

> ⚠️ 数据安全表必须与实际行为一致。如果声明不联网但 AndroidManifest 声明了 INTERNET 权限，Google 会标记。如果确实不联网，建议从 AndroidManifest 移除 INTERNET 权限。

### 第 4 步：内容分级（Content Rating）

Play Console → App Content → Content Rating → 填写 IARC 问卷：

| 问题 | 回答 |
|------|------|
| App contains violence? | No |
| App contains sexual content? | No |
| App references drugs, alcohol, tobacco? | **Yes**（涉及吸烟主题，但目的是戒烟） |
| App is designed for children? | No（13+） |
| App contains gambling? | No |

分级结果通常为：**Everyone** 或 **Teen**（因涉及烟草主题）

### 第 5 步：目标受众与内容（Target Audience）

| 问题 | 回答 |
|------|------|
| Target audience | 18+（成年人） |
| Appeal to children? | No |

### 第 6 步：政府应用 / 广告 / 数据删除

| 问卷项 | 回答 |
|--------|------|
| Government app? | No |
| Contains ads? | No |
| Data deletion | 提供 in-app data deletion（应用内可清除记录 + 退出登录） |

### 第 7 步：上传 AAB

Play Console → Production → Create release：
1. 上传 `app-release.aab`
2. 填写 Release notes（英文）
3. 提交审核

**审核时间**：新应用 3-7 天；已上架应用更新 1-3 天

---

## 五、Apple App Store 上架流程

### 第 1 步：App Store Connect 创建 App

App Store Connect → My Apps → 「+」→ New App：

| 字段 | 填写内容 |
|------|----------|
| Name | SmokeFree |
| Primary Language | English (U.S.) |
| Bundle ID | 选择你在 Developer Portal 注册的 App ID |
| SKU | smokefree001（内部标识，不公开） |
| User Access | Full Access |

### 第 2 步：App 信息

| 字段 | 要求 |
|------|------|
| Name | SmokeFree（最多 30 字符） |
| Subtitle | Quit Smoking Tracker（最多 30 字符） |
| Primary Category | Health & Fitness |
| Secondary Category | Lifestyle |
| Privacy Policy URL | 填入你的域名链接 |
| Bundle ID | 对应 Xcode 中的 Bundle Identifier |

### 第 3 步：隐私标签（App Privacy）

App Store Connect → App Privacy → 「Get Started」：

| 问题 | 回答 |
|------|------|
| Data collected? | Yes |
| Data types | Health & Fitness → Other User Content（吸烟记录） |
| Used for tracking? | No |
| Linked to user? | No（数据不与身份关联） |
| Used for advertising? | No |

> Apple 的隐私标签会展示在 App Store 页面上（如 "No Data Collected" 或 "Data Not Collected from You"）。

### 第 4 步：截图与预览

Apple 要求按设备尺寸提供截图，**至少一组**：

| 设备类型 | 尺寸 | 数量 |
|----------|------|------|
| iPhone 6.7" (15 Pro Max) | 1290×2796 | 至少 1 张，推荐 5-6 张 |
| iPhone 6.5" (11 Pro Max) | 1242×2688 | 至少 1 张（如 6.7" 已提供则可选） |
| iPad 12.9" | 2048×2732 | 如支持 iPad 则必须 |

- 支持 PNG/JPEG，无透明通道
- 可用模拟器截图，也可上传设计图
- 建议添加英文文案说明

**App Preview 视频**（可选但推荐）：
- 15-30 秒，展示核心功能
- 尺寸同截图

### 第 5 步：版本信息

| 字段 | 填写内容 |
|------|----------|
| Version | 1.0.0 |
| Copyright | 2026 [你的名字/公司名] |
| Description | 英文为主（最多 4000 字符） |
| Keywords | quit smoking, smoke free, quit tracker, health（最多 100 字符） |
| Support URL | 你的域名支持页面链接 |
| Marketing URL | 可选，填你的域名首页 |
| Age Rating | 填写问卷 → 通常 12+（因涉及烟草主题） |

**Age Rating 问卷关键项**：
- Cartoon/Fantasy Violence: None
- Realistic Violence: None
- Tobacco Reference: **Infrequent/Mild**（涉及吸烟内容）→ 12+
- Gambling: None
- Unrestricted Web Access: No

### 第 6 步：构建与上传

1. Xcode → Product → Archive
2. Organizer → Distribute App → App Store Connect → Upload
3. 等待 Apple 处理（15-30 分钟）
4. App Store Connect → 选择构建版本 → 提交审核

### 第 7 步：提交审核

- 检查所有信息完整
- 点击 「Submit for Review」
- **审核时间**：通常 24-48 小时（首次可能 2-4 天）

**Apple 审核重点注意事项：**
1. **登录问题**：如果提供第三方登录（Google 等），**必须**同时提供 Sign in with Apple
2. **账户删除**：Apple 要求应用必须提供**完整的账户删除功能**（不能只有退出登录）
3. **HealthKit**：如果声明使用 HealthKit 但实际不使用，会被拒
4. **隐私政策**：必须可访问，且包含 Apple 要求的所有内容
5. **截图真实性**：截图必须与实际应用一致，不能是设计稿

---

## 六、截图制作清单

### 通用截图内容（建议 6 张）

| 序号 | 内容 | 英文文案 |
|------|------|----------|
| 1 | 首页 — 戒烟天数大数字 | "Track every smoke-free day" |
| 2 | 进度页 — 圆环进度+四宫格数据 | "See your progress in real-time" |
| 3 | 打卡页 — 今日打卡 | "Daily check-in keeps you on track" |
| 4 | 历史页 — 趋势图 | "Visualize your smoking trends" |
| 5 | 进度页 — 身体恢复里程碑 | "Watch your body heal" |
| 6 | 我的页 — 省钱统计 | "Save money while saving your health" |

### 各平台尺寸要求

**Google Play：**
- App icon: 512×512 px
- Feature graphic: 1024×500 px
- Phone screenshots: 1080×1920 或 1440×2560（至少 2 张）

**Apple App Store：**
- App icon: 1024×1024 px（在 Xcode 中通过 Asset Catalog 设置）
- iPhone 6.7" screenshots: 1290×2796
- iPhone 6.5" screenshots: 1242×2688
- iPad 12.9" screenshots: 2048×2732（如支持）

---

## 七、常见拒审原因与对策

### Google Play

| 拒审原因 | 对策 |
|----------|------|
| 包名 `com.example.*` | 改为真实域名包名 |
| 隐私政策无法访问 | 确保隐私政策 URL 可公开访问 |
| 数据安全表与实际不符 | INTERNET 权限与"不收集数据"声明冲突 → 移除不需要的权限 |
| 目标 API 级别过低 | 确保 `targetSdk` ≥ 34（2025 年要求） |
| 截图与实际不符 | 使用真实截图或模拟器截图 |
| 违反 Health 相关政策 | 声明"本应用不提供医疗诊断或治疗建议" |

### Apple App Store

| 拒审原因 | 对策 |
|----------|------|
| 缺少 Sign in with Apple | 去掉第三方登录，或添加 Apple 登录 |
| 缺少账户删除功能 | 添加"删除账户"按钮（不只是退出登录） |
| 隐私标签不准确 | 确保隐私标签与实际数据收集行为一致 |
| Health 类应用声明医疗功能 | 明确声明"非医疗应用，不提供诊断建议" |
| 截图缺少必要设备尺寸 | 补充 6.7" iPhone 截图 |
| Support URL 不可访问 | 确保支持页面 URL 可访问 |

---

## 八、上架后维护

| 事项 | 说明 |
|------|------|
| 版本更新 | Google Play: 上传新 AAB + Release notes；Apple: Archive + 提交新版本 |
| 用户评论 | 两个商店都应定期回复评论 |
| 崩溃监控 | Google: Firebase Crashlytics（免费）；Apple: Xcode Organizer 内置 |
| 多语言维护 | 新增功能时同步更新所有语言 strings.xml |
| 隐私政策更新 | 如新增第三方 SDK 或改变数据收集行为，必须更新隐私政策 |

---

## 九、上架前 Checklist（打印对照）

### 代码层面
- [ ] 包名已从 `com.example.smokefree` 改为真实域名包名
- [ ] `targetSdk` ≥ 34
- [ ] 移除不需要的 INTERNET 权限（如果确实不联网）
- [ ] strings.xml 已添加英文版（values-en/）
- [ ] 登录方式已适配全球用户（去掉微信/改为邮箱或去掉登录）
- [ ] 应用内添加"删除账户/清除数据"功能（Apple 强制要求）

### 隐私政策
- [ ] 隐私政策已上传到你的域名，可公开访问
- [ ] 包含联系方式（邮箱）
- [ ] 包含数据保留期限说明
- [ ] 包含 GDPR 用户权利清单
- [ ] 包含 CCPA "不出售数据"声明
- [ ] 包含儿童隐私声明（13 岁以下）

### Google Play
- [ ] Google Play 开发者账号已注册（$25）
- [ ] AAB 已用上传密钥签名
- [ ] Play App Signing 已启用
- [ ] 数据安全表已填写
- [ ] 内容分级问卷已完成
- [ ] 应用图标 512×512 已上传
- [ ] Feature Graphic 1024×500 已上传
- [ ] 截图至少 2 张已上传
- [ ] 商店详情（英文）已填写
- [ ] 隐私政策 URL 已填写

### Apple App Store
- [ ] Apple Developer 账号已注册（$99/年）
- [ ] iOS 工程已创建/适配
- [ ] Distribution Certificate 已创建
- [ ] App ID 已注册
- [ ] Provisioning Profile 已创建
- [ ] 隐私标签已填写
- [ ] 内容分级问卷已完成
- [ ] 应用图标 1024×1024 已设置
- [ ] iPhone 6.7" 截图已准备
- [ ] Support URL 可访问
- [ ] 提供"删除账户"功能（Apple 强制）
- [ ] 如有第三方登录，已添加 Sign in with Apple

---

## 附录 A：各平台费用对比

| 平台 | 注册费 | 年费 | 审核时间 | 软著要求 |
|------|--------|------|----------|----------|
| Google Play | $25（一次性） | 免费 | 3-7 天 | 不需要 |
| Apple App Store | 免费 | $99/年 | 24-48 小时 | 不需要 |
| 小米/华为等国内 | 免费 | 免费 | 1-3 天 | **必须** |

## 附录 B：应用商店关键词建议

**English:**
```
quit smoking, smoke free, stop smoking, quit smoking tracker, smoking cessation, nicotine free, quit habit, health tracker, habit breaker, cigarette counter
```

**中文:**
```
戒烟,戒烟助手,控烟,健康,习惯养成,戒烟打卡,省钱,健康管理
```

**日本語:**
```
禁煙,禁煙アプリ,喫煙,タバコ,健康的な生活,節約
```

**한국어:**
```
금연,금연 앱,흡연,건강,습관 관리
```

**Español:**
```
dejar de fumar, sin humo, dejar el tabaco, salud, contador de cigarrillos
```
