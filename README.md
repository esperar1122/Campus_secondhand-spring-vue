# 🎓 校园二手交易平台

轻量化校园二手交易平台 — 基于 Spring Boot 3 + Vue 3 构建的校内二手商品买卖、交易、评价一体化系统。

---

## 📑 目录

1. [环境要求与安装](#1-环境要求与安装)
2. [获取项目代码](#2-获取项目代码)
3. [数据库初始化](#3-数据库初始化)
4. [后端启动](#4-后端启动)
5. [前端启动](#5-前端启动)
6. [打开网站](#6-打开网站)
7. [常见问题排查](#7-常见问题排查)
8. [项目结构与 API](#附录)
9. [技术栈](#附录)

---

## 1. 环境要求与安装

在开始之前，你的电脑需要安装以下 4 个软件。如果你不确定是否已安装，请逐一检查。

### 1.1 安装 JDK 17

JDK 是 Java 开发工具包，后端程序需要它才能运行。

**检查是否已安装：**
- 按 `Win + R`，输入 `cmd`，回车打开命令提示符
- 输入以下命令并回车：
```bash
java -version
```
- 如果显示类似 `openjdk version "17.0.x"` 或 `java version "17.0.x"`，说明已安装 JDK 17，跳过此步骤
- 如果显示 `java 不是内部或外部命令` 或版本低于 17，需要安装

**安装方法：**
1. 打开浏览器，访问 https://adoptium.net/download/
2. 选择 **Temurin 17 (LTS)**，操作系统选 **Windows**，架构选 **x64**
3. 下载 `.msi` 安装包，双击运行
4. 安装时勾选 **"Set JAVA_HOME variable"** 和 **"Add to PATH"**
5. 一路点击 Next 直到完成
6. **关闭当前的命令提示符，重新打开一个新的**，再次输入 `java -version` 确认安装成功

### 1.2 安装 Maven（可选，推荐）

Maven 是 Java 项目构建工具。如果你使用 IDE（如 IntelliJ IDEA），可以跳过此步骤。
如果只有命令行，则需要安装。

**检查是否已安装：**
```bash
mvn -version
```
- 如果显示 `Apache Maven 3.x.x`，说明已安装
- 如果显示 `mvn 不是内部或外部命令`，需要安装

**安装方法（Windows）：**
1. 浏览器访问 https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**
3. 解压到 `C:\Program Files\apache-maven`（或其他你喜欢的路径）
4. 将 `bin` 目录的完整路径（如 `C:\Program Files\apache-maven\bin`）添加到系统环境变量 PATH：
   - 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
   - 在"系统变量"中找到 `Path`，双击 → 新建 → 粘贴路径 → 确定
5. **重新打开命令提示符**，输入 `mvn -version` 确认安装成功

> **提示**：如果不想折腾环境变量，最简单的方式是使用 **IntelliJ IDEA** 打开 `backend` 文件夹，IDE 内置了 Maven，无需额外配置。

### 1.3 安装 MySQL 8.0

MySQL 是数据库，所有数据都存储在这里。

**检查是否已安装：**
```bash
mysql -u root -p
```
输入密码后回车。如果能进入 `mysql>` 命令行，说明已安装，输入 `exit` 退出。
如果显示 `mysql 不是内部或外部命令`，需要安装。

**安装方法（推荐使用免安装版）：**
1. 浏览器访问 https://dev.mysql.com/downloads/mysql/8.0.html
2. 选择 **Windows (x86, 64-bit), ZIP Archive** 版本下载
3. 解压到 `C:\mysql`（或其他路径）
4. 以管理员身份打开命令提示符（按 Win 键，输入 cmd，右键选择"以管理员身份运行"）
5. 依次执行以下命令：
```bash
cd C:\mysql\bin
mysqld --initialize-insecure --console
mysqld --install
net start mysql
```
6. 设置 root 密码为 `666999`（与项目配置文件一致）：
```bash
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY '666999';
FLUSH PRIVILEGES;
exit
```
7. 验证：`mysql -u root -p666999` 能进入即成功

> **如果你已有 MySQL 且密码不是 `666999`**：记下你的密码，后面需要修改后端配置文件。

### 1.4 安装 Node.js 18+

Node.js 是前端运行时环境。

**检查是否已安装：**
```bash
node -v
npm -v
```
- 如果 `node -v` 显示 `v18.x.x` 或以上，说明已安装
- 如果显示 `node 不是内部或外部命令`，需要安装

**安装方法：**
1. 浏览器访问 https://nodejs.org
2. 下载 **LTS 版本**（左侧绿色按钮），推荐 v18 或 v20
3. 双击 `.msi` 安装包，一路 Next（全部默认选项即可）
4. **重新打开命令提示符**，输入 `node -v` 确认安装成功

---

## 2. 获取项目代码

### 方法一：直接下载（推荐给不熟悉 Git 的人）
1. 将整个项目文件夹 `Campus_secondhand-spring-vue` 复制到你的电脑上
2. 记住你放置的路径，例如 `D:\Campus_secondhand-spring-vue`

### 方法二：通过 Git 克隆
```bash
git clone <仓库地址>
cd Campus_secondhand-spring-vue
```

---

## 3. 数据库初始化

### 3.1 确保 MySQL 正在运行

打开命令提示符，输入：
```bash
mysql -u root -p666999
```

如果提示 `ERROR 1045`（密码错误），说明你的 MySQL root 密码不是 `666999`，请用你自己的密码替换：
```bash
mysql -u root -p你的密码
```

如果提示 `ERROR 2003`（无法连接到 MySQL），说明 MySQL 服务未启动，执行：
```bash
net start mysql
```

### 3.2 创建数据库并导入表结构

进入 MySQL 命令行后（看到 `mysql>` 提示符），依次执行：

```sql
-- 第 1 步：创建数据库
CREATE DATABASE IF NOT EXISTS campus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 第 2 步：切换到该数据库
USE campus_db;

-- 第 3 步：现在打开另一个命令提示符窗口，用 source 命令导入建表脚本
-- 在 MySQL 命令行中输入（注意：路径改成你自己的实际路径）：
source D:/Campus_secondhand-spring-vue/backend/src/main/resources/schema.sql;
```

> **注意**：路径中使用正斜杠 `/`，不要用反斜杠 `\`。

### 3.3 验证数据库是否创建成功

在 `mysql>` 命令行中输入：
```sql
SHOW TABLES;
```

你应该看到以下 5 张表：

```
+---------------------+
| Tables_in_campus_db |
+---------------------+
| item_images         |
| items               |
| orders              |
| reviews             |
| users               |
+---------------------+
```

输入 `exit` 退出 MySQL。

### 3.4 ⚠️ 重要：修改图片字段类型

由于图片以 Base64 格式存储，默认的 `VARCHAR(255)` 不够用，需要改为 `LONGTEXT`：

```bash
mysql -u root -p666999 -e "ALTER TABLE campus_db.item_images MODIFY COLUMN image_url LONGTEXT NOT NULL COMMENT '图片Base64数据';"
```

---

## 4. 后端启动

### 4.1 确认配置文件正确

用记事本（或任何文本编辑器）打开以下文件：

```
backend\src\main\resources\application.yml
```

**检查并修改数据库密码**（第 11 行附近）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 666999        # ← 改成你的 MySQL root 密码
```

如果你的 MySQL 密码就是 `666999`，则无需修改。

完整配置文件内容如下（供参考）：

```yaml
server:
  port: 8090

spring:
  application:
    name: campus-secondhand-platform
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 666999
  servlet:
    multipart:
      max-file-size: 4MB
      max-request-size: 10MB

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: CampusSecondhandPlatformSecretKey2026VeryLongSecretKeyForJWTTokenGeneration
  expiration: 604800000
```

### 4.2 启动后端 —— 方法 A：使用命令行 Maven

打开命令提示符，进入 `backend` 目录：

```bash
cd D:\Campus_secondhand-spring-vue\backend
```

**首次启动需要先编译**（以后再次启动只需执行第二步）：

```bash
# 第 1 步：编译打包（首次或代码有修改时执行）
mvn clean package -DskipTests

# 第 2 步：启动
mvn spring-boot:run
```

> **如果提示 `mvn 不是内部或外部命令`**：说明 Maven 未配置到 PATH。你可以：
> - 方案 A：找到 Maven 的安装路径，用完整路径执行，例如：
>   ```bash
>   D:\apache-maven-3.9.11\bin\mvn spring-boot:run
>   ```
> - 方案 B（推荐）：使用 IntelliJ IDEA 打开 `backend` 文件夹（见方法 B）

### 4.3 启动后端 —— 方法 B：使用 IntelliJ IDEA（推荐给新手）

1. 下载安装 IntelliJ IDEA Community（免费版）：https://www.jetbrains.com/idea/download/
2. 打开 IDEA → 点击 **Open** → 选择 `backend` 文件夹 → OK
3. IDEA 会自动识别为 Maven 项目，等待右下角进度条跑完（首次打开需要下载依赖，可能需要几分钟）
4. 在左侧项目树中找到：`src/main/java/com/campus/secondhand/SecondhandApplication.java`
5. 右键点击该文件 → **Run 'SecondhandApplication'**
6. 底部控制台出现 `Started SecondhandApplication` 字样即启动成功

### 4.4 验证后端是否启动成功

后端默认运行在 **8090** 端口。

打开浏览器，访问：http://localhost:8090/api/items?page=1&size=5

如果看到返回了 JSON 数据（哪怕是空的），说明后端启动成功。

如果浏览器显示"无法访问"，说明后端未启动成功，请参考[常见问题排查](#7-常见问题排查)。

---

## 5. 前端启动

**重要**：前端和后端需要**同时运行**。请保持后端运行，再**另外打开一个新的命令提示符窗口**来启动前端。

### 5.1 安装依赖（仅首次需要）

打开新的命令提示符，进入 `frontend` 目录：

```bash
cd D:\Campus_secondhand-spring-vue\frontend
npm install
```

等待依赖安装完成（可能会下几百个包，需要几分钟）。

> 如果安装过程中报错，可以尝试清空缓存后重装：
> ```bash
> npm cache clean --force
> npm install
> ```

### 5.2 确认代理配置正确

用记事本打开 `frontend\vite.config.js`，确认内容如下：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8090',   // ← 这里的端口号必须和后端一致
        changeOrigin: true
      }
    }
  }
})
```

> `target` 地址中的 `8090` 就是后端端口。如果你修改了后端端口，这里也要对应修改。

### 5.3 启动前端

```bash
npm run dev
```

启动成功后，你会看到类似以下输出：

```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

### 5.4 ⚠️ 端口冲突处理

如果启动时提示 `Port 3000 is in use`（端口被占用），说明 3000 端口已被其他程序使用。解决方法：

**方法一**：先尝试关闭占用端口的程序：
```bash
netstat -ano | findstr :3000
```
记下输出的 PID（最后一列数字），然后：
```bash
taskkill /PID 那个数字 /F
```
再重新执行 `npm run dev`。

**方法二**：如果不知道是什么程序占用，可以换一个端口。修改 `vite.config.js`：
```javascript
server: {
    port: 3001,   // ← 改成 3001 或其他未被占用的端口
    // ...
}
```
以后访问前端就用新端口。

---

## 6. 打开网站

### 6.1 访问地址

确保后端和前端都在运行，然后打开浏览器访问：

> **http://localhost:3000**

你会看到登录页面。

### 6.2 注册第一个用户

1. 点击登录页面上的 **"还没有账号？立即注册"** 链接
2. 填写信息：
   - 用户名：4～16 个字符
   - 密码：6～20 个字符，需包含字母和数字（例如 `test1234`）
3. 点击注册，成功后会自动跳转到登录页面
4. 输入用户名和密码登录

### 6.3 快速体验流程

```
注册账号 → 登录 → 发布商品 → 换一个浏览器注册另一个账号 →
用第二个账号浏览首页 → 点击购买 → 选择交易方式 → 确认下单 →
用第一个账号登录 → 我的订单 → 我卖出的 → 确认订单 →
用第二个账号 → 我的订单 → 我买到的 → 确认收货 → 评价
```

### 6.4 管理员账号

如果你需要管理员权限，可以手动在数据库中修改：

```sql
mysql -u root -p666999
USE campus_db;
UPDATE users SET role = 1 WHERE username = '你的用户名';
```

管理员登录后可以看到"管理后台"菜单，能够封禁/解封用户、查看仪表盘。

---

## 7. 常见问题排查

### ❌ 问题 1：后端启动报 `Communications link failure`

```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**原因**：MySQL 服务未启动，或连接信息配置错误。

**解决步骤**：
1. 确认 MySQL 是否运行：
   ```bash
   net start mysql
   ```
   如果提示"服务名无效"，说明 MySQL 未安装，请回到[1.3 安装 MySQL](#13-安装-mysql-80)
   
2. 确认连接信息是否正确：
   - 端口默认是 `3306`
   - 用户名默认是 `root`
   - 在命令行用 `mysql -u root -p666999` 测试能否连接

### ❌ 问题 2：后端启动报 `Unknown database 'campus_db'`

**原因**：数据库未创建。

**解决**：回到[第 3 步：数据库初始化](#3-数据库初始化)。

### ❌ 问题 3：前端页面打开，但数据加载不出来

**可能原因 A**：后端没有运行。

**检查**：在浏览器中直接访问 http://localhost:8090/api/items?page=1&size=5
- 如果能打开看到数据 → 后端正常，问题在前端代理
- 如果打不开 → 后端未启动，回到[第 4 步](#4-后端启动)

**可能原因 B**：前端代理端口配置不对。

**检查**：打开 `frontend/vite.config.js`，确认 `proxy` 中的 `target` 端口与后端 `application.yml` 中 `server.port` 一致。如果不一致，修改 `vite.config.js` 后重新执行 `npm run dev`。

### ❌ 问题 4：注册/登录失败

**可能原因**：`item_images` 表字段太小，导致数据库操作异常。这是因为图片以 Base64 存储，单张图片可达几十 KB。

**解决**：确保执行了[第 3.4 步](#34--重要修改图片字段类型)：
```bash
mysql -u root -p666999 -e "ALTER TABLE campus_db.item_images MODIFY COLUMN image_url LONGTEXT NOT NULL COMMENT '图片Base64数据';"
```

### ❌ 问题 5：图片不显示

**原因**：同上，或是前端的 `image_url` 字段访问方式不正确。

**说明**：后端返回的 `images` 字段是一个对象数组 `[{id, itemId, imageUrl}]`，前端应使用 `item.images[0].imageUrl` 来获取图片 URL。此问题已在最新代码中修复。

### ❌ 问题 6：端口被占用

**症状**：启动时提示 `Port xxxx is in use` 或 `Address already in use`。

**解决**：
```bash
# 查看哪个进程占用了端口（以 8090 为例）
netstat -ano | findstr :8090

# 记下输出的最后一列数字（PID），然后强制结束该进程
taskkill /PID 占用的PID /F
```

### ❌ 问题 7：`npm install` 很慢或失败

**解决**：使用国内镜像源加速：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

---

## 附录

### 🛠 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前端** | Vue 3 (Composition API) | ^3.4.0 |
| | Vite | ^5.0.0 |
| | Element Plus | ^2.5.0 |
| | Pinia | ^2.1.7 |
| | Vue Router | ^4.2.5 |
| | Axios | ^1.6.2 |
| **后端** | Spring Boot | 3.2.0 |
| | MyBatis-Plus | 3.5.5 |
| | Spring Security + JWT | jjwt 0.12.3 |
| **数据库** | MySQL | 8.0 |

### ✨ 功能模块

- **用户模块**：注册/登录（JWT 认证）、信用评分体系、信用等级展示
- **商品模块**：发布/编辑/下架商品、Base64 图片存储、分页搜索、商品状态机
- **交易模块**：下单购买（面交/自提/送货上门）、订单状态流转、我买到的/我卖出的
- **评价模块**：交易完成后双方互评（1-5 星）、信用分联动
- **消息通知**：实时通知（新订单、交易完成）、通知中心（已读/删除）
- **管理后台**：用户管理（封禁/解封）、仪表盘统计

### 📁 项目结构

```
Campus_secondhand-spring-vue/
├── backend/                                    # Spring Boot 后端
│   ├── pom.xml                                 # Maven 依赖配置
│   └── src/main/
│       ├── java/com/campus/secondhand/
│       │   ├── SecondhandApplication.java      # 启动入口
│       │   ├── common/Result.java              # 统一响应体
│       │   ├── config/                         # JWT、Security、MyBatis-Plus 配置
│       │   ├── controller/                     # REST 控制器
│       │   │   ├── AuthController.java         # 注册/登录
│       │   │   ├── ItemController.java         # 商品 CRUD
│       │   │   ├── OrderController.java        # 订单管理
│       │   │   ├── ReviewController.java       # 评价管理
│       │   │   ├── MessageController.java      # 留言板
│       │   │   ├── NotificationController.java # 通知中心
│       │   │   ├── UserController.java         # 用户信息
│       │   │   └── AdminController.java        # 管理后台
│       │   ├── dto/                            # 请求对象
│       │   ├── entity/                         # 数据库实体
│       │   ├── mapper/                         # MyBatis-Plus Mapper
│       │   ├── security/                       # JWT 认证过滤器
│       │   └── service/                        # 业务逻辑
│       └── resources/
│           ├── application.yml                 # 应用配置
│           └── schema.sql                      # 建表脚本
├── frontend/                                    # Vue 3 前端
│   ├── vite.config.js                          # Vite 配置（API 代理）
│   └── src/
│       ├── App.vue
│       ├── main.js                             # 入口
│       ├── api/index.js                        # 全部 API 封装
│       ├── router/index.js                     # 路由 + 导航守卫
│       ├── stores/user.js                      # Pinia 用户状态
│       └── views/                              # 页面组件
│           ├── Login.vue / Register.vue         # 登录/注册
│           ├── Home.vue                         # 首页（商品列表）
│           ├── ItemDetail.vue                   # 商品详情
│           ├── Publish.vue / EditItem.vue       # 发布/编辑商品
│           ├── MyItems.vue                      # 我的商品
│           ├── Orders.vue                       # 我的订单
│           ├── Chat.vue                         # 聊天
│           ├── Review.vue                       # 评价
│           ├── Profile.vue                      # 个人中心
│           └── Admin.vue                        # 管理后台
└── README.md
```

### 📡 API 接口速查

#### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |

#### 商品
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/items` | 商品列表（分页+搜索） |
| GET | `/api/items/{id}` | 商品详情 |
| POST | `/api/items` | 发布商品 |
| PUT | `/api/items/{id}` | 编辑商品 |
| PUT | `/api/items/{id}/offline` | 下架商品 |
| GET | `/api/items/my` | 我的商品 |

#### 订单
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/buyer` | 我买到的 |
| GET | `/api/orders/seller` | 我卖出的 |
| PUT | `/api/orders/{id}/confirm` | 确认订单（卖家） |
| PUT | `/api/orders/{id}/reject` | 拒绝订单（卖家） |
| PUT | `/api/orders/{id}/cancel` | 取消订单（买家） |
| PUT | `/api/orders/{id}/complete` | 确认收货（买家） |

#### 评价 / 留言 / 通知
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reviews` | 创建评价 |
| GET | `/api/reviews/user/{userId}` | 用户评价列表 |
| POST | `/api/messages` | 发表留言 |
| GET | `/api/messages/item/{itemId}` | 商品留言 |
| GET | `/api/notifications` | 通知列表 |
| GET | `/api/notifications/unread-count` | 未读数量 |
| PUT | `/api/notifications/{id}/read` | 标记已读 |
| PUT | `/api/notifications/read-all` | 全部已读 |
| DELETE | `/api/notifications/{id}` | 删除通知 |

#### 管理后台（需管理员权限）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 仪表盘 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/ban` | 封禁用户 |
| PUT | `/api/admin/users/{id}/unban` | 解封用户 |

### 🗄 数据库表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `users` | 用户 | id, username, password(Bcrypt), role, credit_score, status |
| `items` | 商品 | id, seller_id, title, description, price, status |
| `item_images` | 商品图片 | id, item_id, image_url(LONGTEXT), sort_order |
| `orders` | 订单 | id, item_id, buyer_id, transaction_type, status, total_price |
| `reviews` | 评价 | id, order_id, reviewer_id, reviewee_id, rating, content |

### 🔐 业务规则

- **信用分**：初始 80 分；交易成功 +2；好评 +3；卖家取消/拒绝 -3；买家取消 -3
- **商品状态**：`0-在售` → `1-已锁定` → `2-已售出`；可手动 `3-下架`
- **订单状态**：`0-待确认` → `1-待交易` → `2-已完成` / `3-已取消`
- **不能购买自己的商品**
- **所有表使用逻辑删除**（`deleted=1` 标记已删除）

### 📝 关键配置位置

| 配置项 | 文件 | 默认值 |
|--------|------|--------|
| 后端端口 | `application.yml` → `server.port` | `8090` |
| MySQL 密码 | `application.yml` → `spring.datasource.password` | `666999` |
| JWT 有效期 | `application.yml` → `jwt.expiration` | `604800000`（7天） |
| 前端端口 | `vite.config.js` → `server.port` | `3000` |
| API 代理目标 | `vite.config.js` → `server.proxy.'/api'.target` | `http://localhost:8090` |
