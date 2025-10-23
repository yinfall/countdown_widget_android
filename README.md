# 倒计时小组件项目说明文档

## 项目简介
本项目为原生安卓倒计时小组件（App Widget），支持在主屏幕添加倒计时卡片，随系统壁纸自动适配主题色，体验类似原生时钟应用。用户可通过设置按钮选择目标日期，组件自动显示距离目标日的剩余天数。

## 主要功能
- **主屏倒计时小组件**：显示当前日期、星期、时间和距离目标日的天数。
- **动态刷新**：进程存活时每秒刷新，进程被杀后依然能每分钟自动刷新，保证时钟和倒计时始终准确。
- **日期选择器**：点击齿轮按钮弹出Material风格日期选择器，主题色自动跟随系统壁纸（Material You动态色）。
- **多实例支持**：可添加多个倒计时小组件，每个可独立设置目标日期。

## 技术实现
- **Kotlin原生开发**，兼容Android 7.0（minSdk 24）及以上。
- **Material Components库**，支持Material3和动态色。
- **AlarmManager定时刷新**，保证组件即使进程被杀也能自动刷新。
- **Handler流畅刷新**，进程活时每秒刷新。
- **SharedPreferences持久化**，每个小组件的目标日期独立保存。
- **RemoteViews渲染**，保证主屏小组件高兼容性。

## 主要文件结构
- `app/src/main/java/com/example/count/CountdownWidgetProvider.kt`：小组件刷新与定时逻辑
- `app/src/main/java/com/example/count/CountdownWidgetConfigureActivity.kt`：配置界面与日期选择器
- `app/src/main/res/layout/countdown_widget.xml`：小组件布局
- `app/src/main/res/xml/countdown_widget_info.xml`：小组件元数据
- `app/src/main/res/values/themes.xml`：主题配置（含动态色）
- `app/src/main/res/values/colors.xml`、`values-v31/colors.xml`：颜色资源

## 构建与运行
1. **环境要求**：
   - JDK 17（AGP 8.x及Kotlin DSL要求）
   - Android Studio
2. **编译命令**：
   ```zsh
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ./gradlew :app:assembleDebug
   ```
3. **安装与测试**：
   - 安装到设备或模拟器后，长按主屏添加倒计时小组件。
   - 点击齿轮按钮选择目标日期，组件自动刷新。

## 常见问题与优化
- **组件不刷新/空白**：已通过AlarmManager兜底刷新，确保组件始终显示内容。
- **设置按钮无响应**：已优化PendingIntent唯一性，保证每个小组件都能正常弹出设置界面。
- **耗电问题**：AlarmManager默认每分钟刷新，已兼顾省电与实时性。如需更高频率可调整，但会增加耗电。

## 扩展建议
- 支持自定义倒计时单位（小时/分钟/秒）
- 支持自定义刷新频率
- 支持自定义小组件样式和字体

## 联系与反馈
如需定制或有其他建议，请联系开发者。

---
当前日期：2025年10月23日

