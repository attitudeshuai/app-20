# 项目20：乐器练习室预约（InstrumentRoom）

## 请帮我从 0 到 1 实现以下小众项目

### 项目概述
为社区文化中心、琴行或学校提供乐器练习室的预约管理。支持房间设备信息、时段预约、使用签到与噪音投诉反馈。

### 创新点 / 小众定位
聚焦"乐器练习室"这一小众场地资源，加入设备清单、练习目标记录、使用排队与邻里噪音协调。

### 目标用户
音乐学习者、琴行、社区文化中心、学校社团

## 项目范围说明
- 本项目为纯后端系统开发，不涉及任何前端页面、UI、CSS/JS 改动。
- 所有功能均通过 RESTful API 对外提供服务，可使用 Postman、curl 或任意 HTTP 客户端进行测试与验收。

## 技术栈（必须严格使用）
- **后端框架**: Java Spring Boot 3.2 (Spring Web)
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **认证**: JWT (Spring Security)
- **API文档**: SpringDoc OpenAPI (Swagger)
- **定时任务**: Spring Scheduler（如需要）
- **容器化**: Docker + Docker Compose
- **测试**: JUnit 5 + Testcontainers（可选）+ Postman 测试集合

## 项目必须包含的交付物
- **Dockerfile**：多阶段构建，基于上述技术栈。
- **docker-compose.yml**：一键启动应用服务 + MySQL 8.0 + 可选管理工具（如 Adminer）。
- **.gitignore**：针对 Java Spring Boot 的标准忽略配置。
- **README.md**：项目简介、目录说明、快速启动、API 文档入口、测试方式。
- **docs/functional_intro.md**：功能说明、ER 图文字描述、核心用例、业务规则。
- **src/**：完整后端源码（Controller / Service / Repository / Entity / DTO / Mapper / Config 等）。
- **tests/**：单元测试 + 集成测试。
- **postman_collection.json**（或同等测试脚本）：覆盖所有接口的功能测试集合。
- **初始化 SQL / Seed Data**：Docker 启动后自动建表并插入示例数据。

## 数据库设计

### 主要数据表
1. **Users** - 用户表
   - Id（主键）
   - Username（用户名，唯一）
   - Email（邮箱，唯一）
   - PasswordHash（密码哈希）
   - Avatar（头像 URL，可选）
   - CreatedAt / UpdatedAt

2. **PracticeRooms** - 练习室
   - Name
   - Location
   - Capacity
   - Equipment
   - HourlyPrice
   - OpenTime
   - CloseTime
   - Status（Open / Closed / Maintenance）
   - CreatedAt

3. **Bookings** - 预约
   - RoomId
   - UserId
   - BookingDate
   - StartTime
   - EndTime
   - Purpose
   - Status（Pending / Confirmed / Cancelled / Completed）
   - CreatedAt

4. **CheckIns** - 签到
   - BookingId
   - CheckInAt
   - CheckOutAt
   - Note

5. **RoomIssues** - 房间问题反馈
   - RoomId
   - ReporterId
   - IssueType
   - Description
   - Status
   - CreatedAt

## 核心功能模块
### 1. 用户认证模块
- 用户注册 / 登录 / JWT 鉴权
- 获取当前登录用户信息

### 2. 练习室管理模块
- 练习室的增删改查（支持分页、搜索、排序）
- 练习室状态/详情/关联操作
- 练习室权限控制（仅所有者或管理员可操作）

### 3. 预约管理模块
- 预约的增删改查（支持分页、搜索、排序）
- 预约状态/详情/关联操作
- 预约权限控制（仅所有者或管理员可操作）

### 4. 签到管理模块
- 签到的增删改查（支持分页、搜索、排序）
- 签到状态/详情/关联操作
- 签到权限控制（仅所有者或管理员可操作）

### 5. 反馈管理模块
- 反馈的增删改查（支持分页、搜索、排序）
- 反馈状态/详情/关联操作
- 反馈权限控制（仅所有者或管理员可操作）

### 6. 统计与搜索模块
- 全局搜索与筛选
- 基础数据看板（数量、趋势、排行榜等）
- 导出关键数据（可选）

## API 接口清单
### Auth
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- GET /api/auth/me - 获取当前用户信息
- PUT /api/auth/me - 更新个人信息

### PracticeRooms（练习室）
- GET /api/practicerooms - 获取练习室列表（支持分页、搜索、筛选）
- POST /api/practicerooms - 创建练习室
- GET /api/practicerooms/{id} - 获取练习室详情
- PUT /api/practicerooms/{id} - 更新练习室
- DELETE /api/practicerooms/{id} - 删除练习室
- PATCH /api/practicerooms/{id}/status - 修改练习室状态

### Bookings（预约）
- GET /api/bookings - 获取预约列表（支持分页、搜索、筛选）
- POST /api/bookings - 创建预约
- GET /api/bookings/{id} - 获取预约详情
- PUT /api/bookings/{id} - 更新预约
- DELETE /api/bookings/{id} - 删除预约
- PATCH /api/bookings/{id}/status - 修改预约状态
- GET /api/bookings/mine - 获取我发布的/关联的预约

### CheckIns（签到）
- GET /api/checkins - 获取签到列表（支持分页、搜索、筛选）
- POST /api/checkins - 创建签到
- GET /api/checkins/{id} - 获取签到详情
- PUT /api/checkins/{id} - 更新签到
- DELETE /api/checkins/{id} - 删除签到

### RoomIssues（反馈）
- GET /api/roomissues - 获取反馈列表（支持分页、搜索、筛选）
- POST /api/roomissues - 创建反馈
- GET /api/roomissues/{id} - 获取反馈详情
- PUT /api/roomissues/{id} - 更新反馈
- DELETE /api/roomissues/{id} - 删除反馈
- PATCH /api/roomissues/{id}/status - 修改反馈状态

### Statistics
- GET /api/stats/overview - 总览统计
- GET /api/stats/trend - 趋势统计（按时间范围）

## Docker 配置要求

### Dockerfile（Java Spring Boot）
```dockerfile
# 阶段1：构建
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 阶段2：运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8090
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3   CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

要求：
1. 使用 Maven 多阶段构建，最终镜像只包含 JRE 与 jar 包。
2. 暴露 8090 端口。
3. 启用 Spring Boot Actuator 健康检查 `/actuator/health`。
4. 通过环境变量读取数据库连接配置。

### docker-compose.yml 要求
```yaml
version: '3.8'
services:
  app:
    build: .
    container_name: instrumentroom_app
    ports:
      - "8090:8090"
    environment:
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=instrumentroom
      - DB_USER=app_user
      - DB_PASSWORD=app_pass
    depends_on:
      mysql:
        condition: service_healthy
  mysql:
    image: mysql:8.0
    container_name: instrumentroom_mysql
    environment:
      - MYSQL_ROOT_PASSWORD=root_pass
      - MYSQL_DATABASE=instrumentroom
      - MYSQL_USER=app_user
      - MYSQL_PASSWORD=app_pass
    ports:
      - "13315:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
  volumes:
    mysql_data:
```

要求：
1. MySQL 使用 8.0 镜像。
2. 应用服务必须等 MySQL healthy 后再启动。
3. 使用 named volume 持久化数据库数据。
4. 环境变量集中管理，禁止在源码中硬编码密码。

## .gitignore 参考
```text
# Java / Maven / Gradle
target/
build/
*.class
*.jar
*.war
*.ear
*.iml

# IDE
.idea/
*.ipr
*.iws
.vscode/

# Secrets & local config
.env
.env.local
application-local.properties
application-dev.properties

# Logs
*.log
logs/

# Test results
surefire-reports/

# OS
Thumbs.db
.DS_Store
```

## 文档要求

### README.md
至少包含：
1. 项目名称与一句话介绍。
2. 功能亮点（3-5 条）。
3. 技术栈说明。
4. 目录结构说明。
5. 快速启动步骤（克隆 → Docker 启动 → 访问接口）。
6. 测试命令与 Postman 集合导入说明。
7. 贡献与许可（可选）。

### docs/functional_intro.md
至少包含：
1. 业务背景与解决的问题。
2. 用户角色与核心用例。
3. 功能模块详细说明。
4. 数据库 ER 图文字描述（表关系）。
5. 关键业务规则（如状态流转、权限规则、时间计算逻辑）。
6. 接口调用示例（至少 3 个）。

## 运行与测试步骤

1. **克隆并进入项目目录**：
   ```bash
   git clone <repo-url>
   cd InstrumentRoom
   ```

2. **Docker 启动**：
   ```bash
   docker-compose up --build -d
   ```

3. **查看日志**：
   ```bash
   docker-compose logs -f app
   ```

4. **验证服务健康**：
   - .NET：`curl http://localhost:8090/health`
   - Java：`curl http://localhost:8090/actuator/health`

5. **导入并执行 Postman 测试集合**，验证所有接口：
   - 注册 / 登录
   - 各实体的 CRUD
   - 搜索 / 筛选 / 分页
   - 统计接口
   - 权限控制（未登录访问受限资源应返回 401）

6. **执行自动化测试**：
   - .NET：`dotnet test`
   - Java：`./mvnw test` 或 `mvn test`

7. **停止服务**：
   ```bash
   docker-compose down -v
   ```

## 其他质量要求
- 使用 Spring Data JPA 操作 MySQL，禁止手写 SQL 进行日常 CRUD（复杂统计可手写）。
- 代码分层清晰，遵循 RESTful API 设计规范。
- 关键代码必须有中文注释，说明业务意图。
- 统一的异常处理与参数校验（.NET FluentValidation / Spring Validation）。
- 使用 JWT 保护敏感接口，未携带 Token 返回 401。
- 数据库连接字符串通过环境变量注入，支持 Docker 内外运行。
- 提供 Seed Data，容器启动后至少有 5-10 条示例数据可用于测试。
- 接口返回统一包装格式（code / message / data）。
- 日志使用框架原生日志（.NET ILogger / SLF4J），记录关键操作与异常。
- 项目必须是小众生活/工作场景，禁止做成通用商城、OA、CMS、ERP。
