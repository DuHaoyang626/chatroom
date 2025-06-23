# 星星点点聊天室项目

## 项目简介
基于Java Spring Boot + WebSocket的即时通讯系统，支持单聊、群聊、小组聊天等功能。

## 项目架构
- **dot-chat-server**: 聊天服务器 (端口8089, WebSocket端口9326)
- **dot-chat-admin**: 管理后台 (端口9089)
- **dot-chat-web**: 前端用户界面
- **nginx**: 反向代理服务器 (端口80)
- **common-util**: 公共工具类

## 快速启动

### 1. 数据库准备
- MySQL (端口3306)
- Redis (端口6379)

### 2. 启动后端服务
```bash
# 启动聊天服务器
cd dot-chat-server
java -jar target/dot-chat-server.jar

# 启动管理后台
cd dot-chat-admin  
java -jar target/dot-chat-admin.jar
```

### 3. 启动nginx
```bash
cd nginx-1.28.0
./nginx.exe
```

### 4. 访问地址
- 用户聊天界面: http://localhost/
- 管理后台: http://localhost/admin/

## 常见问题解决

### Bean创建循环依赖错误

**问题现象**: 启动时出现`BeanCreationException`错误，提示`Error creating bean with name 'chatFriendApplyController': Injection of resource dependencies failed`

**错误信息示例**:
```
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'chatFriendApplyServiceImpl': Injection of resource dependencies failed
...
Relying upon circular references is discouraged and they are prohibited by default. Update your application to remove the dependency cycle between beans.
```

**问题原因**: Spring Boot 2.6+版本默认禁止循环引用，而项目中的Service层存在循环依赖

**解决方案**:
1. **配置允许循环引用** - 在`application.yml`中添加：
```yaml
spring:
  main:
    allow-circular-references: true
```

2. **添加TransactionTemplate Bean配置** - 在`CommBeanConfig.java`中添加：
```java
@Bean
public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
}
```

3. **使用@Lazy注解解决循环依赖** - 在有循环依赖的地方使用：
```java
@Resource
@Lazy
private ChatSubgroupService chatSubgroupService;
```

### 登录502错误问题

**问题现象**: 点击登录按钮后无响应，浏览器控制台出现502 (Bad Gateway)错误

**问题原因**: Spring Boot静态资源处理器配置错误，使用`/**`匹配所有路径导致API请求被错误处理

**解决方案**: 
1. 修改`dot-chat-server/src/main/java/com/dot/comm/config/WebConfig.java`
2. 修改`dot-chat-admin/src/main/java/com/dot/comm/config/WebConfig.java`

将以下配置：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    // ...
}
```

修改为：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 只处理静态资源，不拦截API请求
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/ico/**").addResourceLocations("classpath:/static/ico/");
    registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
}
```

**修复后需要**:
1. 重新编译并启动两个后端服务
2. 确认端口监听状态：
   - 8089端口：聊天服务器
   - 9089端口：管理后台
   - 9326端口：WebSocket服务

### 登录API地址配置问题

**问题现象**: 前端登录时出现502错误，API请求失败

**问题原因**: 前端JavaScript中的API地址配置与实际服务端口不匹配

**解决方案**: 
通过Nginx反向代理实现 `http://localhost` 访问：

1. **恢复前端配置** - 将 `dot-chat-web/src/main/webapp/js/base.js` 中的HOST改回：
```javascript
HOST = "localhost";  // 不带端口号
BASE_URL = "http://" + HOST + "/";
```

2. **启动Nginx** - 使用已配置好的nginx.conf：
```bash
cd nginx-1.28.0
.\nginx.exe
```

3. **启动后端服务**：
```bash
# 聊天服务器 (端口8089)
cd dot-chat-server
mvn spring-boot:run

# 管理后台 (端口9089) - 可选
cd dot-chat-admin  
mvn spring-boot:run
```

**Nginx代理配置说明**:
- `http://localhost/` → 用户聊天界面
- `http://localhost/admin/` → 管理后台界面  
- `http://localhost/api/sys/` → 管理后台API (代理到9089端口)
- `http://localhost/api/` → 聊天API (代理到8089端口)
- WebSocket连接代理到9326端口

### 端口检查命令
```bash
# Windows PowerShell
netstat -an | Select-String "LISTENING" | Select-String ":80|:8089|:9326"

# 检查Java进程
Get-Process -Name "java" -ErrorAction SilentlyContinue
```

**最终访问地址**: `http://localhost` (使用账号 `18805250558`，密码 `666666`)

### 小组聊天问题修复

#### 1. "未知用户"显示问题修复

**问题现象**: 小组聊天中发送者显示为"未知用户"

**问题原因**: 
1. SQL查询中的字段别名使用下划线命名（`sender_nickname`），但Java实体类使用驼峰命名（`senderNickname`）
2. MyBatis字段映射配置不一致

**解决方案**:
1. **修改SQL查询别名** - 在 `ChatSubgroupMsgDao.java` 中统一使用驼峰命名：
```sql
SELECT msg.*, u.nickname as senderNickname, u.avatar as senderAvatar 
FROM chat_subgroup_msg msg 
LEFT JOIN chat_user u ON msg.send_user_id = u.id
```

2. **确保MyBatis配置** - 在 `application.yml` 中已配置：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

3. **前端兜底处理** - 如果后端没有返回昵称，从本地群成员列表获取：
```javascript
if (!senderName && !isOwn) {
    let groupMemberList = getLocalGroupMemberList();
    if (groupMemberList) {
        let sender = groupMemberList.find(member => member.userId === msg.sendUserId);
        if (sender) {
            senderName = sender.nickname;
        }
    }
}
```

#### 2. 小组聊天窗口工具栏图标问题修复

**问题现象**: 小组聊天窗口底部意外显示表情、照片、文件图标

**问题原因**: 
1. 存在冲突的 `subgroup-chat.js` 文件，可能包含旧版本的小组聊天实现
2. CSS样式冲突或JavaScript代码冲突

**解决方案**:
1. **删除冲突文件** - 移除 `dot-chat-web/src/main/webapp/js/subgroup-chat.js`
2. **提高小组聊天窗口层级** - 修改CSS：
```css
.subgroup-chat-window {
    z-index: 9999; /* 原来是1000 */
    overflow: hidden; /* 防止内容溢出 */
}
```

3. **确保功能纯净** - 小组聊天窗口仅支持文本消息，不包含表情、图片、文件功能

## 功能特性
- ✅ 用户注册登录
- ✅ 单聊/群聊
- ✅ 文件传输
- ✅ 语音/视频通话
- ✅ 群内小组聊天
- ✅ 消息推送
- ✅ 管理后台

## 小组聊天功能说明

### 业务规则
1. **一人一组原则**: 每个群成员只能参加一个小组
2. **最小组建规模**: 创建小组时至少选择1个成员，加上创建者就是2人小组
3. **组长权限**: 
   - 小组创建者自动成为组长
   - 组长可以解散小组
   - 组长不能退出小组（只能解散）
4. **成员权限**:
   - 普通成员可以退出小组
   - 成员退出后可以加入其他小组

### 创建流程
1. 群成员点击"创建小组"
2. 输入小组名称（2-20个字符）
3. 至少选择1个群成员进行邀请
4. 系统自动将创建者加入小组
5. 向被邀请成员发送小组邀请
6. 被邀请成员接受邀请后，小组正式成立

### 界面优化
- 使用微信绿色主题（#07c160）替代原有蓝色
- 组长显示特殊标识"(组长)"
- 组长和普通成员显示不同的操作按钮
- 小组聊天窗口仅支持文本消息，不支持表情、图片、文件
- 添加友好的提示信息
- 点击"小组聊天"直接进入"我的小组"界面，简化操作流程

## 技术栈
- **后端**: Spring Boot, MyBatis Plus, TIO WebSocket
- **前端**: jQuery, HTML5, CSS3
- **数据库**: MySQL, Redis
- **代理**: Nginx

## 开发者
- 作者: 吴安然
- 联系方式: 请通过项目Issues联系

## 许可证
Apache License 2.0 