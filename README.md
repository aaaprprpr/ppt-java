# MyPPT 幻灯片编辑器

MyPPT 是一个基于Java Swing开发的简易幻灯片编辑器，支持创建和编辑演示文稿，具有类似PowerPoint的基本功能。

## 项目文件结构

```
src/
├── main.main.Main.java                  # 程序入口点
├── model/                     # 数据模型层
│   ├── Document.java          # 文档模型
│   ├── Page.java             # 页面模型
│   └── PPTObject.java        # 所有PPT对象的基类
│       ├── PPTText.java      # 文本对象模型
│       ├── PPTRect.java      # 矩形对象模型
│       ├── PPTCircle.java    # 圆形对象模型
│       ├── PPTLine.java      # 直线对象模型
│       └── PPTImage.java     # 图片对象模型
├── service/                  # 服务层
│   └── EditorService.java    # 编辑器核心服务
├── io/                       # 输入输出处理
│   ├── PPTFileReader.java    # PPT文件读取器
│   └── PPTFileWriter.java    # PPT文件写入器
├── ui/                       # 用户界面层
│   ├── AppFrame.java         # 主应用程序窗口
│   ├── menu/                 # 菜单相关组件
│   │   ├── MainMenuBar.java  # 主菜单栏
│   │   └── ToolBarPanel.java # 工具栏面板
│   ├── canvas/               # 画布相关组件
│   │   ├── CanvasPanel.java  # 主画布面板
│   │   ├── drawable/         # 可绘制对象
│   │   │   ├── Drawable.java        # 可绘制对象接口
│   │   │   ├── DrawableFactory.java # 可绘制对象工厂
│   │   │   ├── DrawableModelMapper.java # 模型与绘制对象映射器
│   │   │   ├── RectDrawable.java    # 矩形绘制实现
│   │   │   ├── CircleDrawable.java  # 圆形绘制实现
│   │   │   ├── LineDrawable.java    # 直线绘制实现
│   │   │   ├── TextDrawable.java    # 文本绘制实现
│   │   │   └── ImageDrawable.java   # 图片绘制实现
│   │   └── tool/             # 工具处理程序
│   │       ├── ToolCreationHandler.java # 对象创建处理器
│   │       └── ObjectTransformHandler.java # 对象变换处理器
│   ├── slide/                # 幻灯片相关组件
│   │   ├── SlideListPanel.java    # 幻灯片列表面板
│   │   └── ThumbnailRenderer.java # 缩略图渲染器
│   ├── slideshow/            # 放映模式组件
│   │   └── SlideShowWindow.java   # 放映窗口
│   └── property/             # 属性面板组件
│       ├── PropertyPanel.java     # 主属性面板
│       ├── ColorUtil.java         # 颜色工具类
│       └── sections/              # 属性面板各部分
│           ├── EmptyPanel.java    # 空属性面板
│           ├── ShapePropertyPanel.java # 形状属性面板
│           └── TextPropertyPanel.java  # 文本属性面板
└── lib/                      # 项目依赖库
```

## 运行环境和依赖

### 运行环境
- Java Development Kit (JDK) 21.0.8
- 支持的操作系统: Windows, macOS, Linux

### 依赖库
- [Lombok]() - 用于简化Java代码
- [Jackson Databind]() - 用于JSON序列化和反序列化
- [FlatLaf]() - 现代Swing外观主题

## 模块功能详解

### 1. Model 层 (数据模型)
定义了整个PPT编辑器的数据结构：

- `Document`: 表示整个文档，包含标题、页面列表等信息
- `Page`: 表示单个幻灯片页面，包含背景色、对象列表等
- `PPTObject`: 所有可绘制对象的基类，定义了基本属性如位置、尺寸等
- 各种具体对象类型：
  - `PPTText`: 文本对象，支持字体、字号、颜色等属性
  - `PPTRect`: 矩形对象，支持填充和描边
  - `PPTCircle`: 圆形对象，支持填充和描边
  - `PPTLine`: 直线对象，支持起点和终点
  - `PPTImage`: 图片对象，支持加载本地图片文件

### 2. Service 层 (服务层)
- `EditorService`: 核心服务类，采用单例模式管理当前打开的文档、资源文件等全局状态

### 3. IO 层 (输入输出)
- `PPTFileReader`: 负责读取.myppt格式文件，解析其中的文档数据和资源文件
- `PPTFileWriter`: 负责将文档数据和资源文件打包保存为.myppt格式文件

### 4. UI 层 (用户界面)
#### 主框架
- `AppFrame`: 主应用程序窗口，整合所有UI组件

#### 菜单系统
- `MainMenuBar`: 主菜单栏，提供文件操作（新建、打开、保存等）和放映功能
- `ToolBarPanel`: 工具栏，提供快速创建各种对象的按钮

#### 画布系统
- `CanvasPanel`: 主画布区域，负责对象的绘制、选择、编辑等操作
- `drawable/`: 实现了各种可绘制对象的具体绘制逻辑
- `tool/`: 提供对象变换和创建工具的处理逻辑

#### 幻灯片管理
- `SlideListPanel`: 左侧幻灯片缩略图列表，支持拖拽排序、新增/删除页面
- `ThumbnailRenderer`: 缩略图渲染器，将页面内容渲染成小尺寸预览图

#### 属性面板
- `PropertyPanel`: 右侧属性面板，根据选中对象显示相应的属性设置选项
- `sections/`: 各种对象类型的属性设置面板

#### 放映模式
- `SlideShowWindow`: 全屏放映窗口，支持键盘和鼠标操作进行幻灯片播放

## 设计思路

MyPPT采用经典的MVC（Model-View-Controller）架构思想，将数据模型、用户界面和业务逻辑分离：

1. **Model层**负责数据结构定义和存储
2. **View层**（UI层）负责用户界面展示和交互
3. **Controller逻辑**分散在各个UI组件和服务类中

项目采用面向对象设计，通过继承和多态机制实现了良好的扩展性。所有可绘制对象都继承自[PPTObject]()，便于统一管理和处理。

文件存储方面，采用ZIP压缩格式将文档JSON数据和资源文件打包存储，既保证了文件的小巧，又方便了资源管理。

## 模块间交互关系

项目各模块之间通过明确定义的接口进行交互：

1. **UI层 ↔ Service层**:
   - UI组件通过[EditorService.getInstance()]()获取当前文档数据
   - 当用户执行操作时（如创建对象、更改属性），UI层会直接修改Service层维护的数据模型
   - [EditorService]()作为中央枢纽，协调不同UI组件之间的状态同步

2. **Service层 ↔ IO层**:
   - 当用户执行保存操作时，[MainMenuBar]()调用[PPTFileWriter.write()]()方法，传入由[EditorService]()管理的文档和资源数据
   - 当用户打开文件时，[MainMenuBar]()调用[PPTFileReader.read()]()方法读取文件，然后将结果传递给[EditorService.openDocument()]()

3. **UI层内部交互**:
   - [AppFrame]()作为主容器协调[SlideListPanel]()、[CanvasPanel]()和[PropertyPanel]()之间的通信
   - 当[SlideListPanel]()中选择页面变更时，会通知[CanvasPanel]()更新显示内容
   - 当[CanvasPanel]()中选中对象或对象属性发生变化时，会通知[PropertyPanel]()更新属性显示

4. **UI层 ↔ Model层**:
   - UI组件直接操作Model对象（如[PPTObject]()的子类）进行增删改查
   - 通过[DrawableFactory]()将Model对象转换为可绘制的UI表示
   - [DrawableModelMapper]()负责保持Model对象和UI表示之间的同步

5. **工具类与核心模块交互**:
   - [ToolCreationHandler]()处理创建新对象的逻辑，同时更新Model层和UI层
   - [ObjectTransformHandler]()处理对象变换操作，修改Model数据并同步到UI

## 功能列表

### 文件操作
- 新建文档
- 打开现有.myppt文件
- 保存文档
- 另存为新文件

### 幻灯片页面管理
- 添加新页面
- 删除页面
- 拖拽调整页面顺序
- 页面背景色设置

### 对象编辑功能
- 创建和编辑文本框
- 创建和编辑矩形
- 创建和编辑圆形
- 创建和编辑直线
- 插入和显示图片
- 对象的选择、移动、缩放
- 文本内容的直接编辑
- 图形的边框颜色设置
- 图形的填充颜色设置（矩形、圆形）
- 文本的颜色、字体、字号设置

### 显示和导航
- 幻灯片缩略图浏览
- 主画布区域编辑
- 属性面板实时调整
- 全屏放映功能
- 放映模式下的导航控制（键盘和鼠标）

## 使用方法

### 启动程序
1. 确保系统已安装JDK 21.0.8
2. 编译并运行[main.main.Main.java]()文件启动程序

### 基本操作
1. **创建对象**:
   - 点击工具栏上的相应按钮（文本框、矩形、圆形、直线、图片）
   - 按照提示在画布上操作（文本框只需点击一次，其他图形需要点击两次确定起始点和终点）

2. **编辑对象**:
   - 单击选择对象
   - 拖拽对象本身可以移动位置
   - 拖拽对象边缘的控制点可以调整大小
   - 在右侧属性面板中可以修改对象的各种属性

3. **编辑文本**:
   - 双击文本对象可以直接在画布上编辑文本内容

4. **页面管理**:
   - 在左侧页面缩略图区域右键可以新增或删除页面
   - 拖拽页面缩略图可以调整页面顺序
   - 单击页面缩略图可以在画布上查看和编辑该页面

5. **文件操作**:
   - 通过顶部菜单栏的"文件"菜单进行新建、打开、保存等操作

6. **放映幻灯片**:
   - 通过顶部菜单栏的"放映"菜单选择"从头放映"或"从当前页放映"
   - 在放映窗口中可以通过方向键、鼠标点击或鼠标滚轮切换幻灯片
   - 按ESC键退出放映

## 文件格式说明

项目使用自定义的`.myppt`文件格式，实际上是一个ZIP压缩包，包含以下内容：
- `document.json`: 文档的JSON格式元数据
- `resources/`: 存放所有嵌入的资源文件（如图片）的目录

## 开发说明

该项目使用了以下技术栈和第三方库：
- Java Swing: 图形用户界面
- Lombok: 减少样板代码（如getter/setter）
- Jackson: JSON序列化/反序列化
- FlatLaf: 现代化的Swing界面主题

