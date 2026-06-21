# 乐器练习室预约管理系统 - 功能说明文档

## 1. 业务背景与解决的问题

### 1.1 业务背景

在社区文化中心、琴行、学校音乐社团等场景中，乐器练习室是一种宝贵且有限的资源。传统的预约方式存在以下痛点：

- **电话/纸笔登记效率低**：容易出现重复预约、信息遗漏，用户体验差
- **资源冲突严重**：多人同时预约同一时段时缺乏有效的冲突检测机制
- **设备管理缺失**：练习室中的钢琴、架子鼓等贵重设备缺乏状态跟踪
- **使用情况不透明**：预约后是否实际到场、使用时长等数据难以统计
- **问题反馈渠道不畅**：用户遇到噪音、设备损坏、卫生问题时缺乏有效的反馈和处理机制
- **数据分析困难**：缺乏对房间利用率、热门时段、用户行为的数据洞察

### 1.2 解决的问题

本系统通过数字化手段，提供以下解决方案：

| 痛点 | 解决方案 |
|------|----------|
| 预约冲突 | 预约创建时自动检测时段重叠，冲突即拒绝 |
| 效率低下 | 线上自助预约，24小时可用，无需人工干预 |
| 设备管理 | 每个房间配置设备清单，支持问题反馈跟踪 |
| 使用追踪 | 签到/签出流程，自动记录实际使用情况 |
| 问题反馈 | 结构化工单系统，状态跟踪从受理到闭环 |
| 数据洞察 | 管理员统计看板，预约趋势+房间热度排行 |

---

## 2. 用户角色与核心用例

### 2.1 用户角色

| 角色 | 权限范围 | 典型用户 |
|------|----------|----------|
| **普通用户 (ROLE_USER)** | 注册登录、浏览练习室、创建预约、签到签出、提交反馈 | 音乐学习者、学生、琴童家长 |
| **管理员 (ROLE_ADMIN)** | 普通用户全部权限 + 练习室管理(增删改) + 预约审核 + 反馈处理 + 统计看板 | 文化中心管理员、琴行经营者、社团负责人 |

### 2.2 核心用例图（文字描述）

```
                          ┌─────────────────────┐
                          │     乐器练习室系统    │
                          └──────────┬──────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    │                                 │
            ┌───────▼───────┐                 ┌───────▼───────┐
            │   普通用户     │                 │    管理员     │
            └───────┬───────┘                 └───────┬───────┘
                    │                                 │
     ┌──────────────┼──────────────┐      ┌───────────┼───────────────┐
     │              │              │      │           │               │
  ┌──▼──┐       ┌───▼──┐      ┌──▼───┐ ┌─▼───┐  ┌───▼────┐   ┌──────▼─────┐
  │注册 │       │浏览  │      │创建  │ │管理 │  │处理    │   │查看统计    │
  │登录 │       │房间  │      │预约  │ │房间 │  │反馈    │   │看板        │
  └──┬──┘       └──┬───┘      └──┬───┘ └─┬───┘  └───┬────┘   └──────┬─────┘
     │             │              │       │           │               │
     │         ┌───▼───┐      ┌──▼────┐  │       ┌───▼──────┐    ┌──▼──────────┐
     │         │查看   │      │签到/  │  │       │修改预约  │    │总览数据      │
     │         │详情   │      │签出  │  │       │状态      │    │预约趋势      │
     │         └───┬───┘      └──┬────┘  │       └──────────┘    │房间排行      │
     │             │              │       │                       │问题统计      │
     │         ┌───▼────┐     ┌──▼─────┐  │                       └─────────────┘
     │         │搜索/   │     │提交    │  │
     │         │筛选    │     │问题反馈│  │
     │         └────────┘     └────────┘  │
     │                                     │
     └─────────────────────────────────────┘
```

---

## 3. 功能模块详细说明

### 3.1 用户认证模块

#### 功能清单
1. **用户注册**：用户名 + 邮箱 + 密码注册，用户名和邮箱全局唯一
2. **用户登录**：支持用户名或邮箱登录，返回 JWT Token（有效期24小时）
3. **获取当前用户**：根据 Token 获取用户基本信息
4. **更新个人信息**：修改用户名、邮箱、密码、头像

#### 技术细节
- 密码使用 BCrypt 加密存储
- JWT Token 采用 HS256 签名算法，密钥长度 ≥ 256 位
- 所有接口（除注册、登录、健康检查、Swagger文档外）均需携带 `Authorization: Bearer <token>` 请求头
- Token 缺失或无效时返回 HTTP 401
- Token 权限不足时返回 HTTP 403

---

### 3.2 练习室管理模块

#### 功能清单
1. **新增练习室**（管理员）：配置名称、位置、容量、设备清单、时薪、开放时段、状态
2. **练习室列表**：支持分页、按名称/位置搜索、按状态筛选、自定义排序字段
3. **练习室详情**：查看单个房间完整信息
4. **编辑练习室**（管理员）：修改任意字段
5. **修改状态**（管理员）：快速切换 OPEN/CLOSED/MAINTENANCE 状态
6. **删除练习室**（管理员）：软删除或物理删除

#### 核心字段说明
| 字段 | 类型 | 说明 |
|------|------|------|
| name | String(100) | 练习室名称，如"钢琴练习室A" |
| location | String(200) | 物理位置，如"一楼101室" |
| capacity | Integer | 最大容纳人数 |
| equipment | TEXT | 设备清单，用文本描述 |
| hourlyPrice | Decimal(10,2) | 每小时费用（元） |
| openTime/closeTime | LocalTime | 每日开放/关闭时间 |
| status | Enum | OPEN(开放) / CLOSED(关闭) / MAINTENANCE(维护中) |

#### 业务规则
- 创建预约时，仅 `OPEN` 状态的房间可被预约
- 编辑时若修改开放时段，需保证 `openTime < closeTime`
- 维护状态建议提前在房间门口张贴通知

---

### 3.3 预约管理模块

#### 功能清单
1. **创建预约**：选择房间 + 日期 + 时段 + 用途说明
2. **预约列表**：分页 + 按用户/房间/状态/日期筛选
3. **我的预约**：快速查看当前用户的所有预约
4. **预约详情**：查看关联房间和用户的完整信息
5. **编辑预约**：修改房间、日期、时段、用途、状态
6. **修改状态**：快速切换 PENDING/CONFIRMED/CANCELLED/COMPLETED
7. **删除预约**：取消预约记录

#### 预约状态流转
```
                    ┌──────────┐
                    │ PENDING  │  创建预约（默认）
                    └────┬─────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              │              ▼
    ┌──────────┐         │        ┌──────────┐
    │CONFIRMED │         │        │CANCELLED │  用户主动取消
    └────┬─────┘         │        └──────────┘
         │               │
         ▼               │
    ┌──────────┐         │
    │COMPLETED│◄─────────┘  签到完成后自动
    └──────────┘             或管理员标记
```

#### 冲突检测逻辑
创建或更新预约时，系统自动执行以下检测：
1. **日期合法性**：预约日期不能早于今天
2. **时段合法性**：`startTime < endTime`
3. **开放时段匹配**：预约时段必须完全落在房间的 `[openTime, closeTime]` 范围内
4. **冲突检测**：查询同一房间、同一日期下，非 CANCELLED 状态的预约是否与当前时段重叠
   - 重叠判定：`(A.startTime < B.endTime) AND (A.endTime > B.startTime)`
   - 存在任何重叠即返回错误"该时段已被预约"

---

### 3.4 签到管理模块

#### 功能清单
1. **创建签到**：关联预约ID，签到时间自动取当前时间
2. **签到列表**：按预约、房间、用户筛选
3. **签到详情**：查看签到/签出时间、备注
4. **签出操作**：更新签到记录的签出时间，自动将预约状态改为 COMPLETED
5. **删除签到**：删除签到记录

#### 业务流程
```
用户到达练习室
       │
       ▼
  创建签到（关联预约）
       │
       ├── 校验：预约状态必须是 PENDING 或 CONFIRMED
       ├── 校验：只能为自己的预约签到（管理员除外）
       ├── 校验：该预约未签到过
       │
       ▼
  签到成功（checkInAt = 当前时间）
       │
       ▼
  用户练习结束
       │
       ▼
  执行签出（PUT /checkins/{id}, checkOut=true）
       │
       ├── 校验：未重复签出
       ├── checkOutAt = 当前时间
       └── 预约状态 = COMPLETED
```

---

### 3.5 反馈管理模块

#### 功能清单
1. **创建反馈**：选择房间 + 反馈类型 + 详细描述
2. **反馈列表**：按房间、报告者、状态、类型筛选
3. **反馈详情**：查看完整工单信息
4. **编辑反馈**：修改类型、描述（报告者或管理员），修改状态（仅管理员）
5. **状态流转**（管理员）：OPEN → IN_PROGRESS → RESOLVED → CLOSED
6. **删除反馈**：报告者或管理员

#### 常见反馈类型
| 类型 | 示例 |
|------|------|
| 噪音问题 | 隔壁装修、其他房间音量过大、隔音效果差 |
| 设备问题 | 钢琴走音、鼓皮破损、麦克风失灵、椅子摇晃 |
| 卫生问题 | 地面脏乱、垃圾桶未清理、有异味 |
| 温度/空调 | 温度过高或过低、空调故障 |
| 照明问题 | 灯光昏暗、灯泡损坏 |
| 其他 | 任何不属于上述类型的问题 |

#### 状态流转
```
   OPEN (待处理)
        │ 管理员受理
        ▼
IN_PROGRESS (处理中)
        │ 问题解决
        ▼
  RESOLVED (已解决)
        │ 用户确认或管理员关闭
        ▼
   CLOSED (已关闭)
```

---

### 3.6 统计与搜索模块

#### 功能清单（仅管理员）

**① 总览统计 `/api/stats/overview`**

返回数据结构：
```json
{
  "totalUsers": 156,          // 注册用户总数
  "totalRooms": 8,            // 练习室总数
  "activeRooms": 6,           // 当前开放的房间数
  "totalBookings": 423,       // 预约总数
  "pendingBookings": 12,      // 待确认预约
  "confirmedBookings": 28,    // 已确认预约
  "completedBookings": 380,   // 已完成预约
  "totalCheckIns": 356,       // 签到总次数
  "totalIssues": 45,          // 反馈工单总数
  "openIssues": 8,            // 待处理工单
  "roomRanking": [            // 房间预约热度排行（Top 10）
    {"roomName": "钢琴练习室A", "bookingCount": 128},
    {"roomName": "架子鼓练习室", "bookingCount": 89}
  ]
}
```

**② 趋势统计 `/api/stats/trend`**

- 默认查询最近 30 天数据
- 支持自定义 `startDate` 和 `endDate` 范围
- 返回每天的预约数量折线图数据
- 无预约的日期自动补 0，便于前端图表绘制

---

## 4. 数据库 ER 图文字描述

### 4.1 表关系总览

```
┌──────────┐       1:N       ┌────────────┐       1:1       ┌───────────┐
│  Users   │────────────────▶│  Bookings  │───────────────▶│ CheckIns  │
└──────────┘                 └──────┬─────┘                └───────────┘
     │                               │
     │                               │ N:1
     │                               ▼
     │                        ┌──────────────┐       1:N       ┌────────────┐
     │                        │ PracticeRooms│───────────────▶│ RoomIssues │
     │                        └──────────────┘                └──────┬─────┘
     │                                                                │
     └────────────────────────────────────────────────────────────────┘
                                      N:1 (Reporter)
```

### 4.2 表详细说明

#### 表 1：Users（用户表）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名（登录用） |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱（登录用，找回密码） |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt 加密后的密码 |
| avatar | VARCHAR(500) | NULL | 头像 URL |
| role | VARCHAR(50) | DEFAULT 'USER' | 角色：USER / ADMIN |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NULL | 最后更新时间 |

#### 表 2：PracticeRooms（练习室表）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NOT NULL | 房间名称 |
| location | VARCHAR(200) | NOT NULL | 物理位置 |
| capacity | INT | NOT NULL | 容纳人数 |
| equipment | TEXT | NULL | 设备清单 |
| hourly_price | DECIMAL(10,2) | NOT NULL | 每小时价格 |
| open_time | TIME | NOT NULL | 开放时间 |
| close_time | TIME | NOT NULL | 关闭时间 |
| status | VARCHAR(20) | NOT NULL | OPEN/CLOSED/MAINTENANCE |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### 表 3：Bookings（预约表）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| room_id | BIGINT | FK → PracticeRooms.id, NOT NULL | 关联房间 |
| user_id | BIGINT | FK → Users.id, NOT NULL | 预约创建者 |
| booking_date | DATE | NOT NULL | 预约日期 |
| start_time | TIME | NOT NULL | 开始时间 |
| end_time | TIME | NOT NULL | 结束时间 |
| purpose | VARCHAR(500) | NULL | 用途说明 |
| status | VARCHAR(20) | NOT NULL | PENDING/CONFIRMED/CANCELLED/COMPLETED |
| created_at | DATETIME | NOT NULL | 创建时间 |

**唯一约束建议**：`(room_id, booking_date, start_time, end_time, status)` 配合应用层冲突检测

#### 表 4：CheckIns（签到表）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| booking_id | BIGINT | FK → Bookings.id, UNIQUE, NOT NULL | 关联预约（一对一） |
| check_in_at | DATETIME | NULL | 签到时间 |
| check_out_at | DATETIME | NULL | 签出时间 |
| note | TEXT | NULL | 备注 |

#### 表 5：RoomIssues（反馈工单表）

| 列名 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| room_id | BIGINT | FK → PracticeRooms.id, NOT NULL | 关联房间 |
| reporter_id | BIGINT | FK → Users.id, NOT NULL | 报告人 |
| issue_type | VARCHAR(50) | NOT NULL | 问题类型 |
| description | TEXT | NOT NULL | 详细描述 |
| status | VARCHAR(20) | NOT NULL | OPEN/IN_PROGRESS/RESOLVED/CLOSED |
| created_at | DATETIME | NOT NULL | 创建时间 |

---

## 5. 关键业务规则

### 5.1 权限规则矩阵

| 操作 | 未登录 | 普通用户 | 管理员 |
|------|--------|----------|--------|
| 注册/登录 | ✅ | - | - |
| 浏览练习室 | ❌ | ✅ | ✅ |
| 创建练习室 | ❌ | ❌ | ✅ |
| 编辑/删除练习室 | ❌ | ❌ | ✅ |
| 创建预约 | ❌ | ✅（任何房间） | ✅ |
| 查看所有预约列表 | ❌ | ✅ | ✅ |
| 查看预约详情 | ❌ | ✅（仅自己的） | ✅ |
| 修改/取消预约 | ❌ | ✅（仅自己的） | ✅ |
| 签到/签出 | ❌ | ✅（仅自己的预约） | ✅ |
| 创建反馈 | ❌ | ✅ | ✅ |
| 修改反馈状态 | ❌ | ❌ | ✅ |
| 统计看板 | ❌ | ❌ | ✅ |

### 5.2 预约时间计算逻辑

1. **冲突判定算法**（De Morgan's Law 简化）
   - 两个时段 [s1, e1] 和 [s2, e2] 重叠的条件：`s1 < e2 AND e1 > s2`
   - 边界值：完全贴合（如 e1 = s2）不算重叠
2. **最小/最大预约时长**：当前无限制，可在后续版本中增加
3. **跨天预约**：不支持，单次预约必须在同一天内

### 5.3 预约超时处理建议

（当前版本尚未实现自动定时任务，可通过 Spring Scheduler 扩展）

- **自动确认**：创建后若 30 分钟内未取消，自动从 PENDING → CONFIRMED
- **自动完成**：预约结束时间过后 2 小时，若仍为 CONFIRMED 且无签到记录，自动标记 COMPLETED
- **爽约记录**：可在 User 表中增加 `noShowCount` 字段，3 次爽约限制预约权限

---

## 6. 接口调用示例

以下示例均使用 `curl`，假设服务运行在 `http://localhost:8090`。

### 示例 1：完整预约流程（注册 → 登录 → 查看房间 → 创建预约）

```bash
# Step 1: 注册新用户
curl -s -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "musicfan",
    "email": "music@example.com",
    "password": "mypassword123"
  }' | jq
```

**返回：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx.yyy",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 10,
      "username": "musicfan",
      "email": "music@example.com",
      "role": "USER"
    }
  }
}
```

```bash
# Step 2: 保存 Token 变量
TOKEN="eyJhbGciOiJIUzI1NiJ9.xxx.yyy"

# Step 3: 浏览可用的练习室（搜索含"钢琴"的房间）
curl -s -X GET "http://localhost:8090/api/practicerooms?name=钢琴&status=OPEN" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.content'
```

**返回：**
```json
[
  {
    "id": 1,
    "name": "钢琴练习室A",
    "location": "一楼101室",
    "capacity": 2,
    "equipment": "雅马哈三角钢琴 x1, 琴凳 x2, 乐谱架 x2",
    "hourlyPrice": 50.00,
    "openTime": "08:00:00",
    "closeTime": "22:00:00",
    "status": "OPEN"
  },
  {
    "id": 2,
    "name": "钢琴练习室B",
    "location": "一楼102室",
    "capacity": 2,
    "hourlyPrice": 40.00,
    "status": "OPEN"
  }
]
```

```bash
# Step 4: 明天下午 2-4 点预约钢琴练习室A
TOMORROW=$(date -d "+1 day" +%Y-%m-%d)
curl -s -X POST http://localhost:8090/api/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"roomId\": 1,
    \"bookingDate\": \"$TOMORROW\",
    \"startTime\": \"14:00:00\",
    \"endTime\": \"16:00:00\",
    \"purpose\": \"肖邦练习曲练习\"
  }" | jq
```

**返回（成功）：**
```json
{
  "code": 200,
  "message": "预约成功",
  "data": {
    "id": 100,
    "roomId": 1,
    "room": { "name": "钢琴练习室A", "...": "..." },
    "bookingDate": "2026-06-21",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "status": "PENDING"
  }
}
```

---

### 示例 2：签到签出流程

```bash
# Step 1: 预约者到达现场，凭预约号 100 签到
curl -s -X POST http://localhost:8090/api/checkins \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 100,
    "note": "准时到达，设备状态良好"
  }' | jq '.data | {id, checkInAt, note}'

# Step 2: 2小时后练习结束，执行签出
curl -s -X PUT http://localhost:8090/api/checkins/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "checkOut": true,
    "note": "练习完成，谢谢"
  }' | jq '.data | {checkInAt, checkOutAt, booking: {booking: .booking.status}}'
```

**签出后返回：**
```json
{
  "checkInAt": "2026-06-21 14:05:23",
  "checkOutAt": "2026-06-21 16:02:45",
  "booking": {
    "booking": "COMPLETED"
  }
}
```

---

### 示例 3：用户反馈噪音问题 + 管理员处理闭环

```bash
# Step 1: 普通用户提交噪音投诉
curl -s -X POST http://localhost:8090/api/roomissues \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 3,
    "issueType": "噪音问题",
    "description": "隔壁小提琴练习室的音量过大，严重影响钢琴练习专注力，希望加强隔音或安排隔音时段"
  }'

# Step 2: 管理员登录
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' | jq -r '.data.token')

# Step 3: 管理员查看所有待处理工单
curl -s -X GET "http://localhost:8090/api/roomissues?status=OPEN" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data.content | length'

# Step 4: 管理员受理，状态改为处理中
curl -s -X PATCH http://localhost:8090/api/roomissues/5/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'

# Step 5: 问题解决后，管理员关闭工单
curl -s -X PATCH http://localhost:8090/api/roomissues/5/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "RESOLVED"}'

echo "工单处理完成！"
```

---

## 附录 A：统一响应格式

所有接口（除 Actuator 外）均采用统一响应格式：

```json
{
  "code": 200,          // 状态码：200成功，4xx客户端错误，5xx服务端错误
  "message": "success", // 可读消息
  "data": { ... }       // 业务数据（对象/数组/分页包装）
}
```

分页列表包装格式：
```json
{
  "content": [ ... ],   // 当前页数据
  "page": 0,            // 当前页码（从0开始）
  "size": 10,           // 每页大小
  "totalElements": 86,  // 总条数
  "totalPages": 9,      // 总页数
  "first": true,        // 是否第一页
  "last": false         // 是否最后一页
}
```
