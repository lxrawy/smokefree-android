# 戒烟助手 - Android 原生应用

## 项目结构

```
android-smokefree/
├── app/
│   └── src/
│       └── main/
│           ├── res/
│           │   ├── layout/           # 布局文件
│           │   │   ├── activity_login.xml       # 登录页面
│           │   │   ├── activity_main.xml        # 主页面（含底部导航）
│           │   │   ├── fragment_home.xml       # 首页
│           │   │   ├── fragment_history.xml    # 历史记录页
│           │   │   ├── fragment_track.xml      # 打卡页
│           │   │   └── fragment_me.xml        # 我的页面
│           │   ├── values/            # 资源文件
│           │   │   ├── colors.xml             # 颜色定义
│           │   │   ├── strings.xml            # 字符串资源
│           │   │   └── styles.xml             # 样式定义
│           │   ├── drawable/         # 可绘制资源
│           │   │   ├── bg_primary_button.xml  # 主按钮背景
│           │   │   ├── bg_header.xml          # 头部渐变背景
│           │   │   ├── bg_input.xml           # 输入框背景
│           │   │   ├── bg_card.xml            # 卡片背景
│           │   │   ├── bg_progress_bar.xml    # 进度条
│           │   │   └── bg_avatar.xml          # 头像背景
│           │   ├── menu/
│           │   │   └── bottom_nav_menu.xml    # 底部导航菜单
│           │   └── color/
│           │       └── bottom_nav_colors.xml   # 底部导航颜色选择器
│           └── AndroidManifest.xml   # (需要创建)
└── README.md               # 本文件
```

## 已完成的工作

### 1. 颜色资源 (colors.xml)
- 完整的粉色主题色系（pink_50 到 pink_900）
- 灰色系（gray_50 到 gray_700）
- 状态颜色（绿色、红色、微信绿等）

### 2. 字符串资源 (strings.xml)
- 所有页面所需的文本内容
- 支持中文本地化

### 3. 布局文件
#### 登录页面 (activity_login.xml)
- 手机号登录/微信登录选项卡
- 手机号输入框 + 验证码发送
- 微信登录按钮
- 用户协议和隐私政策提示

#### 主页面 (activity_main.xml)
- 顶部头部显示坚持天数和已省金额
- FrameLayout 作为 Fragment 容器
- 底部导航栏（首页、回顾、打卡、我）

#### 首页 (fragment_home.xml)
- 天数计数器（大号数字显示）
- 激励语卡片
- 今日情况统计（4个网格卡片）
- 健康恢复时间线
- 快速记录按钮

#### 历史页面 (fragment_history.xml)
- 吸烟历史总结卡片
- 本周吸烟趋势图（柱状图）
- 每日记录表格

#### 打卡页面 (fragment_track.xml)
- 天数计数器
- 目标设置/显示区域
- 今日打卡（加减按钮记录吸烟数量）
- 成就徽章展示

#### 我的页面 (fragment_me.xml)
- 用户信息头部（登录/未登录状态）
- 吸烟历史信息设置
- 提醒设置
- 其他功能入口（数据导出、帮助、关于）
- 退出登录按钮

### 4. Drawable 资源
- 渐变背景
- 圆角卡片
- 自定义进度条
- 按钮背景选择器

## 下一步需要完成的工作

### 1. 创建 Kotlin/Java 代码文件

#### 必要 Activity/Fragment：
- `LoginActivity.kt` - 处理登录逻辑
- `MainActivity.kt` - 主页面，管理底部导航和 Fragment 切换
- `HomeFragment.kt` - 首页逻辑
- `HistoryFragment.kt` - 历史页面逻辑
- `TrackFragment.kt` - 打卡页面逻辑
- `MeFragment.kt` - 我的页面逻辑

#### 功能实现：
- 本地数据存储（SharedPreferences 或 Room 数据库）
- 计时器和提醒功能
- 图表绘制（可使用 MPAndroidChart 库）
- 登录逻辑（手机号验证码、微信登录）

### 2. 添加依赖 (build.gradle)

```gradle
dependencies {
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'  // 图表库
    
    // 微信登录 SDK（如果需要）
    // implementation 'com.tencent.mm.opensdk:wechat-sdk-android:6.8.0'
}
```

### 3. 创建 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        
        <activity
            android:name=".LoginActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity
            android:name=".MainActivity"
            android:exported="false" />
            
    </application>
</manifest>
```

### 4. 图标和资源
- 创建应用启动图标（mipmap 文件夹）
- 底部导航图标（需要矢量图标或 PNG 图标）
- 状态栏图标

## 与原 HTML 应用的对应关系

| HTML 元素 | Android 实现 |
|-----------|-------------|
| `<div class="phone-mockup">` | `activity_main.xml` + Fragments |
| 登录覆盖层 | `LoginActivity` |
| 底部导航 | `BottomNavigationView` |
| 卡片样式 | `LinearLayout` + `bg_card.xml` |
| 渐变头部 | `bg_header.xml` 渐变背景 |
| 输入框 | `EditText` + `bg_input.xml` |
| 按钮 | `Button` + `bg_primary_button.xml` |
| 网格统计 | `GridLayout` |
| 时间线 | `LinearLayout` + 自定义布局 |
| 表格 | `LinearLayout` 水平/垂直组合 |

## 使用建议

1. **图标处理**：由于 Android 不支持直接在 XML 中使用 emoji 作为图标，建议：
   - 使用 Material Icons 或自定义图标
   - 或者使用图标库（如 Android-Iconics）

2. **底部导航图标**：需要创建 `drawable` 资源文件，或者使用 Vector Asset Studio 生成矢量图标

3. **图表功能**：
   - 柱状图推荐使用 MPAndroidChart 库
   - 或者自定义 View 绘制

4. **数据存储**：
   - 简单数据使用 SharedPreferences
   - 复杂数据（历史记录）使用 Room 数据库

## 注意事项

1. 原 HTML 中的 emoji（如 🚭 📊 ✅）在 Android 中需要替换为：
   - 文本表情（如 "戒烟"）
   - 或者图标资源

2. 底部导航的图标需要在 `res/menu/bottom_nav_menu.xml` 中引用图标资源，而不是 emoji

3. 需要添加运行时权限（如需要通知提醒功能）

## 快速开始

1. 将 `android-smokefree` 文件夹复制到你的 Android 项目目录
2. 在 Android Studio 中打开项目
3. 同步 Gradle 依赖
4. 创建缺失的 Kotlin/Java 文件
5. 运行应用到模拟器或真机

---

转换完成！如有任何问题，欢迎随时询问。
