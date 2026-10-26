# java大作业 PPT 幻灯片编辑器

基于 Java 21 和 Swing 的课程项目，支持文本、矩形、圆形、直线、图片、多页编辑和全屏放映。文档使用自定义的 `.myppt` 格式，不支持直接读写 PowerPoint 的 `.ppt/.pptx`。

## 运行

安装 JDK 21，在项目目录执行：

```powershell
.\gradlew.bat run
```

构建包含运行依赖的 JAR：

```powershell
.\gradlew.bat build
java -jar build/libs/ppt.jar
```

首次构建需要联网下载 Gradle 和依赖。依赖版本统一声明在 `build.gradle`，无需手动维护 `lib` 目录。macOS/Linux 使用 `./gradlew`。

## 代码结构

```
src/main/java/myppt/
├── App.java             程序入口
├── model/               文档、页面和图形数据；保持 .myppt 的类型标识
├── editor/              当前文档、图片资源、修改状态和几何操作
├── io/                  ZIP/JSON 文档读写，完整写入后替换旧文件
├── render/              画布、缩略图和放映共用的绘制逻辑
└── ui/
    ├── AppFrame.java    窗口组装、文件菜单和文档切换
    ├── ToolBarPanel.java
    ├── canvas/          鼠标操作、选中、缩放和文本编辑
    ├── property/        图形和文本属性
    └── slides/          页面列表、排序和全屏放映

src/test/java/myppt/     文件兼容、编辑状态、几何操作及桌面流程测试
```

模型是对象状态的唯一来源。界面通过构造参数和回调协作，不使用全局窗口或编辑器单例。渲染器直接读取模型，图片解码结果按当前文档缓存。

## 操作

- 先新建或打开文档。新建文档会选择保存位置并创建第一页。
- 文本工具点击一次创建文本；矩形、圆形、直线点击两次确定位置。Esc 取消绘图。
- 单击选择对象，拖动对象移动，拖动控制点缩放；直线使用两个端点。Delete 删除选中对象。
- 双击文本编辑，支持多行。点击其他位置或 Ctrl+Enter 提交，Esc 放弃本次文字修改。切页、保存和放映前会提交正在编辑的文字。
- 右侧修改颜色、填充、字体和字号。左侧页面右键可以插入、删除页面和设置背景色，拖动页面可以排序。
- Ctrl+滚轮缩放，Ctrl+0 恢复适应窗口，Alt+滚轮横向滚动。
- Ctrl+N 新建，Ctrl+O 打开，Ctrl+S 保存，Ctrl+Shift+S 另存为。
- F5 从头放映，Shift+F5 从当前页放映；方向键、滚轮和鼠标左键翻页，Esc 退出。
- 文档有未保存修改时，关闭或切换文档会提示保存。

## 验证

```powershell
.\gradlew.bat test
.\gradlew.bat desktopTest
```

`test` 检查旧格式兼容、保存失败保护、图片资源、页面顺序、对象删除、文本切页和几何操作。`desktopTest` 需要桌面会话，会使用临时文件自动验证新建、编辑、插图、保存、重新打开和放映，结束后关闭测试窗口。

报告位于 `build/reports/tests/`；桌面测试截图位于 `build/reports/desktop/editor.png`。

## 文件格式与打包

`.myppt` 是 ZIP，包含 UTF-8 编码的 `document.json` 及 `resources/` 下的图片。继续使用原有的 `text/rect/circle/line/image` 类型标识，可读取旧版文档；旧文件里未实现功能使用的遗留字段会被忽略。

如需生成自带 Java 运行环境的 Windows 应用，在构建 JAR 后调用 JDK 的 `jpackage`（按本机安装位置调整路径）：

```powershell
& 'C:\Program Files\Java\jdk-21\bin\jpackage.exe' --input build/libs --dest dist --name MyPPT --main-jar ppt.jar --main-class myppt.App --type app-image
```
