# InstrumentRoom - 乐器练习室预约管理系统

为社区文化中心、琴行或学校提供乐器练习室的预约管理。支持房间设备信息、时段预约、使用签到与噪音投诉反馈。

## ✨ 功能亮点

- **练习室资源管理**：管理多种类型练习室（钢琴、小提琴、架子鼓等），配置设备清单、容纳人数、时薪价格
- **智能预约系统**：时段冲突检测、预约状态流转（待确认→已确认→已完成/已取消）
- **签到签出流程**：二维码/预约号签到，自动关联预约状态，记录使用时长
- **问题反馈机制**：噪音投诉、设备损坏、卫生问题等多类型反馈工单系统
- **权限分级管理**：普通用户/管理员双角色体系，JWT Token 安全认证
- **数据统计看板**：预约趋势、房间热度排行、问题处理进度可视化

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| 数据库 | MySQL | 8.0 |
| 认证 | Spring Security + JWT | 6.x |
| API文档 | SpringDoc OpenAPI (Swagger) | 2.5.0 |
| 构建工具 | Maven | 3.9 |
| JDK | Eclipse Temurin JDK | 17 |
| 容器化 | Docker + Docker Compose | - |

## 📁 目录结构

```
instrument-room/
├── src/
│   ├── main/
│   │   ├── java/com/instrumentroom/
│   │   │   ├── controller/          # REST 控制器层
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── PracticeRoomController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   ├── CheckInController.java
│   │   │   │   ├── RoomIssueController.java
│   │   │   │   └── StatsController.java
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── PracticeRoomService.java
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── CheckInService.java
│   │   │   │   ├── RoomIssueService.java
│   │   │   │   └── StatsService.java
│   │   │   ├── repository/          # 数据访问层
│   │   │   ├── entity/              # 实体类
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   ├── config/              # 配置类
│   │   │   ├── security/            # 安全相关（JWT）
│   │   │   ├── exception/           # 异常处理
│   │   │   └── InstrumentRoomApplication.java
│   │   └── resources/
│   │       └── application.yml      # 应用配置
│   └── test/                        # 单元测试 & 集成测试
├── docs/
│   └── functional_intro.md          # 功能详细说明文档
├── Dockerfile                       # Docker多阶段构建
├── docker-compose.yml               # Docker Compose编排
├── pom.xml                          # Maven依赖配置
├── .gitignore
├── postman_collection.json          # Postman接口测试集合
└── README.md
```

## 🚀 快速启动

### 前置条件

- Docker Engine 20.10+
- Docker Compose v2.0+

### 一键启动

```bash
# 1. 克隆项目（如已下载则跳过）
git clone <repo-url>
cd instrument-room

# 2. Docker 构建并启动服务
docker-compose up --build -d

# 3. 查看启动日志
docker-compose logs -f app

# 4. 等待服务健康检查通过（约30秒）
curl http://localhost:8090/actuator/health
```

### 访问服务

| 服务 | 地址 | 说明 |
|------|------|------|
| API 根路径 | http://localhost:8090 | 后端服务端口 8090 |
| Swagger UI | http://localhost:8090/swagger-ui.html | 在线接口文档与调试 |
| OpenAPI JSON | http://localhost:8090/api-docs | OpenAPI 3.0 规范 |
| 健康检查 | http://localhost:8090/actuator/health | Spring Boot Actuator |
| Adminer | http://localhost:8080 | MySQL 数据库管理工具 |
| MySQL | localhost:13315 | 数据库对外端口 |

### 默认测试账号

容器启动后自动初始化以下测试数据：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 拥有全部权限，包括统计看板 |
| 普通用户 | zhangsan | password123 | 张三 |
| 普通用户 | lisi | password123 | 李四 |
| 普通用户 | wangwu | password123 | 王五 |
| 普通用户 | zhaoliu | password123 | 赵六 |

同时初始化 8 间练习室、6 条预约记录、2 条签到记录、5 条反馈工单。

## 🧪 测试

### Postman 集合测试

1. 打开 Postman → Import → 选择项目根目录下的 `postman_collection.json`
2. 确认 `baseUrl` 变量为 `http://localhost:8090`
3. 按顺序运行集合中的请求，或使用 Collection Runner 一键执行
4. 每个请求均包含断言脚本，验证状态码和返回数据

测试覆盖：
- ✅ 认证模块（注册、登录、权限校验）
- ✅ 练习室 CRUD + 搜索筛选 + 权限控制
- ✅ 预约创建 + 冲突检测 + 状态流转
- ✅ 签到/签出流程
- ✅ 问题反馈工单系统
- ✅ 管理员统计看板
- ✅ 健康检查与文档端点

### Maven 单元测试

```bash
# 运行全部测试
mvn test

# 运行指定测试类
mvn test -Dtest=AuthServiceTest
```

测试使用 H2 内存数据库，无需依赖 MySQL。

### 手动测试示例（curl）

```bash
# 1. 登录获取 Token
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  | jq -r '.data.token')

# 2. 调用受保护接口（获取练习室列表）
curl -X GET http://localhost:8090/api/practicerooms?page=0&size=10 \
  -H "Authorization: Bearer $TOKEN"

# 3. 创建练习室（管理员权限）
curl -X POST http://localhost:8090/api/practicerooms \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试练习室",
    "location": "测试楼101",
    "capacity": 2,
    "hourlyPrice": 50.00,
    "openTime": "08:00:00",
    "closeTime": "22:00:00"
  }'
```

## 🔌 API 接口概览

### 认证模块 (Auth)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/register | 用户注册 | 公开 |
| POST | /api/auth/login | 用户登录 | 公开 |
| GET | /api/auth/me | 获取当前用户信息 | 已登录 |
| PUT | /api/auth/me | 更新个人信息 | 已登录 |

### 练习室 (PracticeRooms)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/practicerooms | 练习室列表（分页+搜索） | 已登录 |
| POST | /api/practicerooms | 创建练习室 | 管理员 |
| GET | /api/practicerooms/{id} | 练习室详情 | 已登录 |
| PUT | /api/practicerooms/{id} | 更新练习室 | 管理员 |
| DELETE | /api/practicerooms/{id} | 删除练习室 | 管理员 |
| PATCH | /api/practicerooms/{id}/status | 修改状态 | 管理员 |

### 预约 (Bookings)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/bookings | 预约列表 | 已登录 |
| POST | /api/bookings | 创建预约 | 已登录 |
| GET | /api/bookings/mine | 我的预约 | 已登录 |
| GET | /api/bookings/{id} | 预约详情 | 所有者/管理员 |
| PUT | /api/bookings/{id} | 更新预约 | 所有者/管理员 |
| DELETE | /api/bookings/{id} | 删除预约 | 所有者/管理员 |
| PATCH | /api/bookings/{id}/status | 修改状态 | 所有者/管理员 |

### 签到 (CheckIns)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/checkins | 签到列表 | 已登录 |
| POST | /api/checkins | 创建签到 | 已登录 |
| GET | /api/checkins/{id} | 签到详情 | 所有者/管理员 |
| PUT | /api/checkins/{id} | 更新（签出） | 所有者/管理员 |
| DELETE | /api/checkins/{id} | 删除签到 | 所有者/管理员 |

### 反馈 (RoomIssues)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/roomissues | 反馈列表 | 已登录 |
| POST | /api/roomissues | 创建反馈 | 已登录 |
| GET | /api/roomissues/{id} | 反馈详情 | 报告者/管理员 |
| PUT | /api/roomissues/{id} | 更新反馈 | 报告者/管理员 |
| DELETE | /api/roomissues/{id} | 删除反馈 | 报告者/管理员 |
| PATCH | /api/roomissues/{id}/status | 修改状态 | 管理员 |

### 统计 (Stats)
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/stats/overview | 总览统计 | 管理员 |
| GET | /api/stats/trend | 趋势统计 | 管理员 |

## ⚙️ 环境变量配置

应用通过环境变量读取配置，支持 Docker 内外灵活部署：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | localhost | MySQL 主机地址 |
| DB_PORT | 3306 | MySQL 端口 |
| DB_NAME | instrumentroom | 数据库名 |
| DB_USER | app_user | 数据库用户名 |
| DB_PASSWORD | app_pass | 数据库密码 |
| JWT_SECRET | (默认值) | JWT 签名密钥，生产环境必须修改 |

## 📝 停止服务

```bash
# 停止服务并保留数据卷
docker-compose down

# 停止服务并删除数据库（慎用）
docker-compose down -v
```

## 📄 详细文档

- 功能说明与业务规则：[`docs/functional_intro.md`](docs/functional_intro.md)

## 🤝 贡献

欢迎提交 Issue 与 Pull Request！

## 📜 License

Apache License 2.0
