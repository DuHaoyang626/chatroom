# 星星点点聊天室（Dot Chat）

## 📋 项目简介
这是一个基于Java+Spring Boot开发的现代化即时通讯系统，支持实时聊天、音视频通话、AI智能助手等功能。项目采用前后端分离架构，具有良好的扩展性和稳定性。

## ✨ 功能特性

### 🔥 新增功能：群内小组聊天
- **小组创建**：群成员可以创建小组并邀请部分群成员
- **消息隔离**：小组内聊天，非小组成员无法收到消息
- **互斥约束**：用户同时只能参与一个小组，保证聊天焦点
- **智能邀请**：支持多个邀请，但只能接受一个
- **实时通知**：邀请、加入、退出的即时推送

### 💬 核心聊天功能
- **单聊**：一对一私人聊天
- **群聊**：多人群组聊天
- **消息类型**：支持文字、图片、文件、表情等
- **消息状态**：已读/未读状态显示
- **历史记录**：完整的聊天记录保存

### 📞 音视频通话
- **语音通话**：高质量音频通话
- **视频通话**：1080P高清视频通话
- **屏幕共享**：桌面分享功能
- **群组通话**：支持多人音视频会议

### 🤖 AI智能助手
- **AI聊天**：集成DeepSeek AI智能对话
- **智能回复**：AI自动生成回复建议
- **语言翻译**：多语言实时翻译
- **内容总结**：长文本智能摘要

### 👥 用户管理
- **好友系统**：添加、删除、分组管理好友
- **用户资料**：头像、昵称、状态设置
- **在线状态**：实时在线/离线状态显示
- **黑名单**：屏蔽不良用户

### 📁 文件传输
- **文件上传**：支持多种文件格式
- **图片预览**：图片自动压缩和预览
- **断点续传**：大文件断点续传
- **云存储**：阿里云OSS文件存储

## 🏗️ 技术栈

### 后端技术
- **Spring Boot 3.3.5** - 主框架
- **MyBatis Plus 3.5.7** - 数据持久化
- **MySQL 8.3.0** - 数据库
- **T-io WebSocket 3.8.6** - 实时通信框架
- **Redis** - 缓存和会话管理
- **Alibaba Druid** - 数据库连接池

### 前端技术
- **jQuery** - JavaScript框架
- **WebSocket** - 实时通信
- **WebRTC** - 音视频通话
- **Bootstrap** - UI框架
- **Layer** - 弹层组件

### 第三方集成
- **阿里云OSS** - 文件存储
- **DeepSeek AI** - AI聊天助手
- **二维码生成** - 扫码功能

## 🚀 快速开始

### 环境要求
- **JDK 21+** 
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Node.js 16+** (可选，用于前端开发)

### 安装步骤

1. **克隆项目**
```bash
git clone https://gitee.com/du-hao-yang/chatroom.git
cd chatroom
```

2. **数据库配置**
```sql
-- 创建数据库
CREATE DATABASE dot_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入基础表结构
source dot-chat-server/sql/聊天室MySQL表结构.sql;

-- 导入小组功能表结构
source dot-chat-server/sql/群内小组功能.sql;
```

3. **配置文件修改**
修改 `dot-chat-server/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dot_chat
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

4. **启动服务**
```bash
# 启动后端服务
cd dot-chat-server
mvn spring-boot:run

# 启动前端服务（新终端）
cd dot-chat-web
mvn spring-boot:run
```

5. **访问应用**
- 聊天室：http://localhost:8081
- 管理后台：http://localhost:8082
- API文档：http://localhost:8080/doc.html

## 📱 使用说明

### 群内小组聊天功能使用

1. **创建小组**
   - 在群聊界面点击"小组聊天"按钮
   - 选择"创建小组"Tab
   - 输入小组名称，选择要邀请的成员
   - 点击"创建小组"完成创建

2. **处理邀请**
   - 收到小组邀请时，按钮会显示红色数字提醒
   - 点击"小组聊天"按钮，选择"邀请处理"Tab
   - 选择"接受"或"拒绝"邀请

3. **小组聊天**
   - 加入小组后，在"当前小组"Tab可以看到小组信息
   - 点击"进入小组模式"开始小组聊天
   - 小组模式下，只有小组成员能收到消息

4. **退出小组**
   - 在"当前小组"Tab点击"退出小组"
   - 确认后即可退出当前小组

### 约束说明
- ⚠️ **重要**：每个用户在同一个群中只能同时参与一个小组
- 可以收到多个小组邀请，但只能接受一个
- 退出小组后可以接受新的邀请或创建新小组

## 🏗️ 项目结构

```
chatroom/
├── dot-chat-server/          # 后端服务器
│   ├── src/main/java/
│   │   └── com/dot/msg/chat/
│   │       ├── controller/   # 控制器层
│   │       ├── service/      # 服务层
│   │       ├── dao/          # 数据访问层
│   │       ├── model/        # 实体类
│   │       └── tio/          # WebSocket处理
│   └── sql/                  # 数据库脚本
├── dot-chat-web/             # 前端Web应用
│   └── src/main/webapp/
│       ├── js/               # JavaScript文件
│       ├── css/              # 样式文件
│       └── pages/            # 页面文件
├── dot-chat-admin/           # 管理后台
├── common-util/              # 公共工具库
└── README.md
```

## 🔧 开发指南

### 新功能开发流程
1. 创建数据库表结构
2. 创建实体类和DAO接口
3. 实现Service业务逻辑
4. 创建Controller API接口
5. 开发前端页面和JavaScript
6. 编写单元测试
7. 更新文档

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的注释模板
- 所有public方法必须有完整注释
- 重要业务逻辑必须有日志记录

## 🐛 问题排查

### 常见问题
1. **无法连接数据库**
   - 检查MySQL服务是否启动
   - 确认数据库连接配置正确
   - 检查防火墙设置

2. **WebSocket连接失败**
   - 检查端口是否被占用
   - 确认T-io配置正确
   - 查看浏览器控制台错误信息

3. **小组功能异常**
   - 确认已执行小组功能SQL脚本
   - 检查数据库约束是否正确创建
   - 查看后端日志错误信息

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 贡献步骤
1. Fork本项目
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交Pull Request

## 📄 更新日志

### v2.0.0 - 2024-12-XX
- ✨ 新增群内小组聊天功能
- 🔒 实现用户小组互斥约束
- 📱 优化前端交互体验
- 🐛 修复已知问题

### v1.0.0 - 2024-01-XX
- 🎉 项目初始版本
- 💬 基础聊天功能
- 📞 音视频通话功能
- 🤖 AI智能助手集成

## 📝 许可证

本项目采用 [MIT许可证](LICENSE)

## 👨‍💻 开发者

- **原作者**：@du-hao-yang
- **功能扩展**：吴安然 (群内小组功能)

## 🙏 致谢

感谢所有为这个项目贡献代码和建议的开发者！
