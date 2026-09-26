# 韧云智护后端 API 接口文档

> [!IMPORTANT]  
> 本文档面向 Web 前端与移动端（APP）开发人员，描述后端已提供的 REST 接口。
>
> **统一响应格式**：所有接口（SSE 流除外）均返回 `ApiResponse<T>` 结构：
>
> ```json
> { "success": true,  "message": "OK",  "data": <T> }
> { "success": false, "message": "<错误信息>", "data": null }
> ```
>
> 前端请始终从 `data` 字段读取业务数据，从 `message` 读取提示信息，以 `success` 判断请求是否成功。

---

## 0. 认证：登录与注册

# 登录
POST /api/v1/auth/portal/login
Content-Type: application/json

### 示例请求
```json
{
  "loginType": "patient",
  "credential": {
    "username": "zhangsan",
    "password": "123abc"
  }
}
```
> `loginType` 可选 `patient` 或 `doctor`，需与账号角色一致。

### 示例返回 （200）：
```json
{
  "success": true,
  "data": {
    "id": 290900625330274304,
    "username": "zhangsan",
    "name": "lotiyu",
    "role": "patient",
    "department": null,
    "createdAt": "2026-09-12T01:35:13.834299"
  }
}
```
### 示例返回 （400）（参数校验失败）
```json
{
  "success": false,
  "message": "loginType不能为空",
  "data": null
}
```
### 示例返回 （401）

```json
{
  "success": false,
  "message": "用户名或密码错误",
  "data": null
}
```
### 示例返回 （403）（角色与登录入口不匹配）
```json
{
  "success": false,
  "message": "当前账号不属于该登录入口",
  "data": null
}
```

### 示例返回 （429）：
```json
{
  "success": false,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

# 注册
POST /api/v1/auth/portal/register
Content-Type: application/json

### 示例请求
```json
{
  "username": "昵称（用于登录）",
  "name": "姓名",
  "gender": "male",
  "password": "123abc"
}
```

### 示例返回 （201）：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 296214463181225984,
    "username": "sd",
    "passwordHash": "$2a$10$ghKhhDM/akPy1xYXnH1aCOcEgRq7TYLUhaFVZeVet1wQU0rCj6du.",
    "age": 0,
    "role": "patient",
    "name": "lotiyu",
    "gender": null,
    "status": "active",
    "createdAt": "2026-09-26T17:30:31.4211398"
  }
}
```
### 示例返回 （400）（参数校验失败）
```json
{
  "success": false,
  "message": "username不能为空",
  "data": null
}
```
### 示例返回 （409）：

```json
{
"success": false,
"message": "用户已注册",
"data": null
}
```

### 示例返回 （429）：
```json
{
  "success": false,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}

```
要点：
- 登录请求体为 `{ "loginType": "patient|doctor", "credential": { "username": "...", "password": "..." } }`，成功后通过 `Set-Cookie` 写入 `access_token`（HttpOnly），前端无需手动处理 token。
- 注册请求体为 `{ "username": "...", "name": "...", "gender": "...", "password": "..." }`。
- 参数校验失败统一返回 HTTP 400，`message` 为第一个校验失败的字段提示，如 `"xxx不能为空"`。

---

## 1. AI 训练康复评估报告

### `POST /api/report`

根据单次训练数据生成 AI 康复评估报告。

**请求示例：**
```http
POST /api/report HTTP/1.1
Content-Type: application/json

{
  "mode": "leg_raise",
  "modeName": "直腿抬高",
  "durationSec": 120.5,
  "samples": 600,
  "stats": {
    "avgPitch": 75.0,
    "maxPitch": 90.0,
    "minPitch": 60.0,
    "avgRoll": 5.0,
    "maxRoll": 10.0,
    "minRoll": 0.0,
    "avgKed": 12.5,
    "standardRatio": 80.0,
    "adjustRatio": 15.0,
    "dangerRatio": 5.0
  }
}
```

**响应示例：**

- **400 Bad Request**（缺少必填字段 `mode` 或 `stats`，或 stats 内字段为空）
  ```json
  { "success": false, "message": "mode不能为空", "data": null }
  ```

- **200 OK**（成功）
  ```json
  {
    "success": true,
    "message": "OK",
    "data": "【训练概况】\n本次直腿抬高训练时长 120.5 秒，完成有效动作 8 次……\n\n【动作评估】\n……\n\n【风险提示】\n……\n\n【康复建议】\n……"
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
- **400 Bad Request**（缺少必要训练数据：`mode` 或 `stats` 为空）
  ```json
  { "success": false, "message": "缺少必要的训练数据", "data": null }
  ```
- **502 Bad Gateway**（服务器未配置 DEEPSEEK_API_KEY）
  ```json
  { "success": false, "message": "服务器未配置 DEEPSEEK_API_KEY", "data": null }
  ```

---

## 2. 患者阶段性康复报告

### `POST /api/patient-report`

基于患者训练记录、任务、打卡记录生成阶段性康复评估报告。

**请求示例：**
```http
POST /api/patient-report HTTP/1.1
Content-Type: application/json

{
  "patientName": "张三",
  "gender": "男",
  "age": 35,
  "surgeryDate": "2026-06-01",
  "doctor": "李医生",
  "status": "康复中",
  "records": [
    { "time": "2026-09-20 10:00", "action": "直腿抬高", "pitch": 85, "abnormal": 1, "status": "达标" }
  ],
  "tasks": [
    { "name": "直腿抬高", "count": 10, "unit": "次", "done": true }
  ],
  "history": [
    { "date": "2026-09-20", "done": true }
  ],
  "streakDays": 5,
  "totalCheckins": 20
}
```

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": "【患者概况】\n患者张三，男，35岁……\n\n【训练表现】\n……\n\n【康复依从性】\n……\n\n【风险提示】\n……\n\n【下一步建议】\n……"
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
- **502 Bad Gateway**（未配置 AI Key）
  ```json
  { "success": false, "message": "服务器未配置 DEEPSEEK_API_KEY", "data": null }
  ```

---

## 3. AI 通用聊天

### `POST /api/chat`

通用聊天代理，透传消息列表给 AI 模型。

**请求示例：**
```http
POST /api/chat HTTP/1.1
Content-Type: application/json

{
  "messages": [
    { "role": "user", "content": "术后多久可以开始直腿抬高训练？" }
  ],
  "temperature": 0.5
}
```

**响应示例：**

- **400 Bad Request**（缺少必填字段，如 `messages`）
  ```json
  { "success": false, "message": "messages不能为空", "data": null }
  ```

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": "术后第 1 天即可开始进行踝泵与股四头肌等长收缩训练……"
  }
  ```
- **400 Bad Request**（缺少消息内容）
  ```json
  { "success": false, "message": "缺少消息内容", "data": null }
  ```
- **502 Bad Gateway**（未配置 AI Key）
  ```json
  { "success": false, "message": "服务器未配置 DEEPSEEK_API_KEY", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
---

## 4. 任务管理

### 4.1 获取患者任务列表

#### `GET /api/tasks/{patientId}`

**请求示例：**
```http
GET /api/tasks/123456789 HTTP/1.1
```

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": 987654321,
        "patientId": 123456789,
        "name": "直腿抬高",
        "count": 10,
        "unit": "次",
        "description": "保持膝关节伸直\n仰卧位，腿抬高45°保持5秒",
        "status": "pending",
        "source": "doctor",
        "createdAt": "2026-09-20T10:00:00"
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 4.2 医生发布/更新任务（按任务名 upsert）

#### `POST /api/tasks/{patientId}`

**请求示例：**
```http
POST /api/tasks/123456789 HTTP/1.1
Content-Type: application/json

{
  "name": "直腿抬高",
  "count": 15,
  "unit": "次",
  "keyPoints": "保持膝关节伸直",
  "details": "仰卧位，腿抬高45°保持5秒"
}
```

**响应示例：**

- **400 Bad Request**（缺少必填字段 `name`）
  ```json
  { "success": false, "message": "name不能为空", "data": null }
  ```

- **200 OK**（创建或更新后返回任务对象）
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "id": 987654321,
      "patientId": 123456789,
      "name": "直腿抬高",
      "count": 15,
      "unit": "次",
      "description": "保持膝关节伸直\n仰卧位，腿抬高45°保持5秒",
      "status": "pending",
      "source": "doctor",
      "createdAt": "2026-09-20T10:00:00"
    }
  }
  ```
  > 说明：若该患者已存在同名任务，则更新原任务的数量、单位、说明并重置为 `pending`；否则新建任务。发布成功后会通过 SSE 推送 `{"type":"new_task","patientId":"..."}`。
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 4.3 更新任务状态

#### `PATCH /api/tasks/{taskId}`

**请求示例：**
```http
PATCH /api/tasks/987654321 HTTP/1.1
Content-Type: application/json

{ "done": true }
```
或
```json
{ "status": "done" }
```

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "id": 987654321,
      "patientId": 123456789,
      "name": "直腿抬高",
      "count": 15,
      "unit": "次",
      "description": "保持膝关节伸直\n仰卧位，腿抬高45°保持5秒",
      "status": "done",
      "source": "doctor",
      "createdAt": "2026-09-20T10:00:00"
    }
  }
  ```
  > 状态变更后会通过 SSE 推送 `{"type":"task_update","taskId":"...","patientId":"...","done":true}`。

- **404 Not Found**（任务不存在）
  ```json
  { "success": false, "message": "任务不存在", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
---

## 5. 消息与实时推送

### 5.1 SSE 实时推送

#### `GET /api/messages/stream`

建立 Server-Sent Events 长连接，用于接收消息、撤回、任务变更等实时事件。

**响应格式（`Content-Type: text/event-stream`）：**

- 连接成功：
  ```
  data: {"type":"connected"}
  ```
- 新消息：
  ```
  data: {"type":"message","msg":{"id":"...","fromRole":"doctor","fromName":"李医生","toPatientId":"123","type":"text","text":"你好","time":"10:00","date":"2026-09-20","read":false,"recalled":false}}
  ```
- 撤回消息：
  ```
  data: {"type":"recall","id":"<消息id>"}
  ```
- 新任务：
  ```
  data: {"type":"new_task","patientId":"<患者id>"}
  ```
- 任务状态变更：
  ```
  data: {"type":"task_update","taskId":"<任务id>","patientId":"<患者id>","done":true}
  ```

> 前端使用 `new EventSource('/api/messages/stream')`，在 `onmessage` 回调中按 `type` 分发处理。

### 5.2 获取消息列表

#### `GET /api/messages?patientId={patientId}`

**请求示例：**
```http
GET /api/messages?patientId=123456789 HTTP/1.1
```
不传 `patientId` 时返回全部消息。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": "msg-uuid-001",
        "fromRole": "doctor",
        "fromName": "李医生",
        "toPatientId": "123456789",
        "type": "text",
        "text": "今天训练感觉如何？",
        "time": "10:00",
        "date": "2026-09-20",
        "read": false,
        "recalled": false,
        "createdAt": "2026-09-20T10:00:00"
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 5.3 发送消息

#### `POST /api/messages`

**请求示例：**
```http
POST /api/messages HTTP/1.1
Content-Type: application/json

{
  "id": "msg-uuid-002",
  "fromRole": "patient",
  "fromName": "张三",
  "toPatientId": "123456789",
  "type": "text",
  "text": "感觉还可以",
  "time": "10:01",
  "date": "2026-09-20",
  "read": false,
  "recalled": false
}
```
> `id` 建议由前端生成（UUID）；若未提供，后端会自动生成。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "id": "msg-uuid-002",
      "fromRole": "patient",
      "fromName": "张三",
      "toPatientId": "123456789",
      "type": "text",
      "text": "感觉还可以",
      "time": "10:01",
      "date": "2026-09-20",
      "read": false,
      "recalled": false,
      "createdAt": "2026-09-20T10:01:00"
    }
  }
  ```
  > 发送成功后会通过 SSE 广播 `{"type":"message","msg":{...}}` 给所有在线客户端。
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 5.4 撤回消息

#### `PATCH /api/messages/{id}/recall`

**请求示例：**
```http
PATCH /api/messages/msg-uuid-002/recall HTTP/1.1
```

**响应示例：**

- **200 OK**（撤回后 `text` 被清空、`recalled` 置为 true）
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "id": "msg-uuid-002",
      "fromRole": "patient",
      "fromName": "张三",
      "toPatientId": "123456789",
      "type": "text",
      "text": "",
      "time": "10:01",
      "date": "2026-09-20",
      "read": false,
      "recalled": true,
      "createdAt": "2026-09-20T10:01:00"
    }
  }
  ```
  > 撤回后会通过 SSE 广播 `{"type":"recall","id":"msg-uuid-002"}`。

- **404 Not Found**（消息不存在）
  ```json
  { "success": false, "message": "消息不存在", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
---

## 6. 医生与患者管理

### 6.1 获取医生列表

#### `GET /api/doctors`

用于注册页选择主治医生。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": 111111111,
        "name": "李医生",
        "hospital": "康复医院",
        "department": "运动医学科",
        "title": "主治医师"
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 6.2 获取待审核患者列表

#### `GET /api/patients/pending`

医生端查看待审核的注册患者。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": 123456789,
        "name": "张三",
        "gender": "男",
        "status": "pending",
        "age": 35,
        "diagnosis": "ACL 术后",
        "hospital": "康复医院",
        "doctorName": "",
        "doctorId": null,
        "createdAt": "2026-09-19T08:00:00"
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 6.3 批准患者

#### `POST /api/patients/{id}/approve`

将患者状态置为 `approved`，绑定主治医生，并自动下发默认康复任务（若该患者尚无任务）。

**请求示例：**
```http
POST /api/patients/123456789/approve HTTP/1.1
Content-Type: application/json

{
  "doctorId": 111111111,
  "doctorName": "李医生"
}
```

**响应示例：**

- **200 OK**
  ```json
  { "success": true, "message": "OK", "data": "已批准" }
  ```
- **404 Not Found**（患者不存在）
  ```json
  { "success": false, "message": "患者不存在", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
### 6.4 拒绝患者

#### `POST /api/patients/{id}/reject`

将患者状态置为 `rejected`。

**请求示例：**
```http
POST /api/patients/123456789/reject HTTP/1.1
```

**响应示例：**

- **200 OK**
  ```json
  { "success": true, "message": "OK", "data": "已拒绝" }
  ```
- **404 Not Found**（患者不存在）
  ```json
  { "success": false, "message": "患者不存在", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
---

## 7. 训练数据上传与 Agent 分析

### `POST /api/v1/training/upload`

上传一次训练数据，后端保存训练记录并自动运行多 Agent 链式分析（训练分析 → 风险评估 → 康复趋势 → 医生总结），返回分析报告与待完成任务列表。

> 需登录，后端从 JWT 自动获取 `patientId`，无需前端传入。

**请求示例：**
```http
POST /api/v1/training/upload HTTP/1.1
Content-Type: application/json

{
  "mode": "leg_raise",
  "modeName": "直腿抬高",
  "durationSec": 120.5,
  "samples": 600,
  "stats": {
    "avgPitch": 75.0,
    "maxPitch": 90.0,
    "minPitch": 60.0,
    "avgRoll": 5.0,
    "maxRoll": 10.0,
    "minRoll": 0.0,
    "avgKed": 12.5,
    "standardRatio": 80.0,
    "adjustRatio": 15.0,
    "dangerRatio": 5.0
  }
}
```

**响应示例：**

- **400 Bad Request**（缺少必填字段 `mode`、`stats` 或 stats 内字段为空）
  ```json
  { "success": false, "message": "mode不能为空", "data": null }
  ```

- **200 OK**（训练数据已保存且 AI 分析完成）
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "trainingId": 555555555,
      "report": {
        "id": 444444444,
        "patientId": 123456789,
        "trainingId": 555555555,
        "trainingAnalysis": "本次训练完成 10 次动作，其中 8 次达标……",
        "riskAssessment": "当前膝关节活动度在安全范围内……",
        "rehabTrend": "与上周相比 ROM 提升 5°，康复趋势良好……",
        "doctorSummary": "建议继续保持当前训练强度……",
        "finalReport": "【综合报告】……",
        "suggestedTasks": "直腿抬高 15次/组、踝泵 20次/组",
        "createdAt": "2026-09-20T10:05:00"
      },
      "suggestedTasks": [
        {
          "id": 987654321,
          "patientId": 123456789,
          "name": "直腿抬高",
          "count": 15,
          "unit": "次",
          "description": "保持膝关节伸直",
          "status": "pending",
          "source": "ai",
          "createdAt": "2026-09-20T10:05:00"
        }
      ]
    }
  }
  ```
- **400 Bad Request**（缺少必填字段 `mode` 或 `stats`，由 `@Valid` 校验触发）
  ```json
  { "success": false, "message": "mode 不能为 null", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```
- **502 Bad Gateway**（训练数据已保存，但 AI 分析失败）
  ```json
  { "success": false, "message": "训练数据已保存，但 AI 分析失败", "data": null }
  ```

---

## 8. 训练历史记录

### `GET /api/v1/training/history?limit={limit}`

获取当前登录用户的最近训练记录。

> 需登录，后端从 JWT 自动获取用户 ID。

**请求示例：**
```http
GET /api/v1/training/history?limit=20 HTTP/1.1
```
`limit` 默认 20，可不传。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": 555555555,
        "mode": "leg_raise",
        "modeName": "直腿抬高",
        "trainingDate": "2026-09-20",
        "durationSec": 120.5,
        "samples": 600,
        "avgPitch": 75.0,
        "maxPitch": 90.0,
        "minPitch": 60.0,
        "avgRoll": 5.0,
        "maxRoll": 10.0,
        "minRoll": 0.0,
        "avgKed": 12.5,
        "standardRatio": 0.8,
        "adjustRatio": 0.15,
        "dangerRatio": 0.05
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```

---

## 9. 最新分析报告

### `GET /api/v1/reports/latest`

获取当前登录用户的最近一条 Agent 分析报告。

> 需登录，后端从 JWT 自动获取用户 ID。

**请求示例：**
```http
GET /api/v1/reports/latest HTTP/1.1
```

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "id": 444444444,
      "patientId": 123456789,
      "trainingId": 555555555,
      "trainingAnalysis": "本次训练完成 10 次动作，其中 8 次达标……",
      "riskAssessment": "当前膝关节活动度在安全范围内……",
      "rehabTrend": "与上周相比 ROM 提升 5°，康复趋势良好……",
      "doctorSummary": "建议继续保持当前训练强度……",
      "finalReport": "【综合报告】……",
      "suggestedTasks": "直腿抬高 15次/组、踝泵 20次/组",
      "createdAt": "2026-09-20T10:05:00"
    }
  }
  ```
- **404 Not Found**（暂无分析报告）
  ```json
  { "success": false, "message": "暂无分析报告", "data": null }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```

---

## 10. ROM 趋势

### `GET /api/v1/users/trend/rom?days={days}`

获取当前登录用户最近 N 天的关节活动度（ROM）趋势数据。

> 需登录，后端从 JWT 自动获取用户 ID。

**请求示例：**
```http
GET /api/v1/users/trend/rom?days=7 HTTP/1.1
```
`days` 默认 7，可不传。

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": {
      "patientId": 123456789,
      "days": 7,
      "points": [
        { "date": "2026-09-14", "rom": 70.0 },
        { "date": "2026-09-15", "rom": 72.0 },
        { "date": "2026-09-16", "rom": 75.0 }
      ],
      "startRom": 70.0,
      "endRom": 75.0,
      "deltaRom": 5.0,
      "trend": "上升"
    }
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```

---

## 11. 当前用户任务列表

### `GET /api/v1/users/tasks`

获取当前登录用户（患者）的全部任务列表。

> 需登录，后端从 JWT 自动获取用户 ID。与 `GET /api/tasks/{patientId}` 功能相同，但无需在路径中传 ID，适合患者端使用。

**请求示例：**
```http
GET /api/v1/users/tasks HTTP/1.1
```

**响应示例：**

- **200 OK**
  ```json
  {
    "success": true,
    "message": "OK",
    "data": [
      {
        "id": 987654321,
        "patientId": 123456789,
        "name": "直腿抬高",
        "count": 15,
        "unit": "次",
        "description": "保持膝关节伸直\n仰卧位，腿抬高45°保持5秒",
        "status": "pending",
        "source": "doctor",
        "createdAt": "2026-09-20T10:00:00"
      }
    ]
  }
  ```
- **401 Unauthorized**（未登录）
  ```json
  { "success": false, "message": "Not Logged in.", "data": null }
  ```

---

## 附：字段说明

### 任务状态（`TaskDTO.status`）

| 值 | 含义 |
|---|---|
| `pending` | 待完成 |
| `done` | 已完成 |

### 消息角色（`fromRole`）

| 值 | 含义 |
|---|---|
| `doctor` | 医生发送 |
| `patient` | 患者发送 |
