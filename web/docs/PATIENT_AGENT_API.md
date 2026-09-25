# 韧小护患者端智能体 API

## 1. 基本信息

- 接口名称：患者端训练数据解读
- 请求方式：`POST`
- 接口路径：`/api/patient-agent`
- 本地地址：`http://localhost:3000/api/patient-agent`
- 请求格式：`application/json`
- 返回格式：`application/json`
- 问题长度限制：最多 500 个字符

DeepSeek API Key 由服务端通过环境变量读取，前端请求中不应携带 DeepSeek Key。

## 2. 请求参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `patientId` | string | 是 | 患者编号，仅用于服务端会话标识，不发送给大模型 |
| `question` | string | 是 | 患者提出的训练问题，最多 500 字符 |
| `conversationId` | string | 否 | 会话编号，用于保持短期对话上下文 |
| `snapshot` | object | 否 | 当前患者的结构化训练数据 |
| `snapshot.patient` | object | 否 | 患者基础信息 |
| `snapshot.recentRecords` | array | 否 | 最近训练记录，最多读取 10 条 |
| `snapshot.tasks` | array | 否 | 医生安排的训练任务 |
| `snapshot.recentCheckins` | array | 否 | 最近打卡记录，最多读取 14 条 |
| `snapshot.streakDays` | number | 否 | 当前连续打卡天数 |

### 训练记录字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `time` | string | 训练时间 |
| `action` | string | 动作名称 |
| `pitch` | number | 现有算法计算的角度，单位为度 |
| `abnormal` | number | 现有算法记录的异常次数 |
| `status` | string | 现有算法给出的训练状态 |

### 任务字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `name` | string | 任务名称 |
| `count` | number | 任务次数或时长 |
| `unit` | string | 单位，例如“次”“秒” |
| `keyPoints` | string | 医生填写的动作要点 |
| `done` | boolean | 是否已完成 |

## 3. 完整请求示例

```http
POST /api/patient-agent HTTP/1.1
Host: localhost:3000
Content-Type: application/json
```

```json
{
  "patientId": "p1",
  "conversationId": "patient_p1_session_001",
  "question": "请根据现有记录解释我今天的训练完成情况",
  "snapshot": {
    "patient": {
      "id": "p1",
      "age": 28,
      "gender": "男",
      "surgeryDate": "2026-08-10",
      "status": "康复中",
      "doctor": "李医生"
    },
    "recentRecords": [
      {
        "time": "2026-09-19 15:00",
        "action": "直腿抬高",
        "pitch": 42,
        "abnormal": 1,
        "status": "良好"
      }
    ],
    "tasks": [
      {
        "name": "直腿抬高",
        "count": 10,
        "unit": "次",
        "keyPoints": "保持膝关节伸直",
        "done": true
      },
      {
        "name": "靠墙静蹲",
        "count": 30,
        "unit": "秒",
        "keyPoints": "膝关节不要超过脚尖",
        "done": false
      }
    ],
    "recentCheckins": [
      { "date": "2026-09-18", "done": true },
      { "date": "2026-09-19", "done": true }
    ],
    "streakDays": 2
  }
}
```

### cURL 示例

```bash
curl -X POST http://localhost:3000/api/patient-agent \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "p1",
    "conversationId": "patient_p1_session_001",
    "question": "请解释我今天的训练完成情况",
    "snapshot": {
      "recentRecords": [
        {
          "time": "2026-09-19 15:00",
          "action": "直腿抬高",
          "pitch": 42,
          "abnormal": 1,
          "status": "良好"
        }
      ],
      "tasks": [
        {
          "name": "直腿抬高",
          "count": 10,
          "unit": "次",
          "done": true
        }
      ],
      "recentCheckins": [
        { "date": "2026-09-19", "done": true }
      ],
      "streakDays": 2
    }
  }'
```

## 4. 成功返回

### HTTP 状态码

```text
200 OK
```

### 返回示例

```json
{
  "answer": "结论：你今天安排的训练任务已部分完成。直腿抬高任务已经完成，靠墙静蹲仍待完成。最近一次直腿抬高记录的角度为42度，系统状态标记为良好，同时记录到1次异常。请继续按照医生规定的次数和动作要点训练，不要自行增加训练量。",
  "mode": "model-assisted",
  "trace": [
    { "tool": "safety", "label": "安全边界检查", "status": "completed" },
    { "tool": "tasks", "label": "核对医生任务", "status": "completed" },
    { "tool": "training", "label": "读取训练记录", "status": "completed" }
  ],
  "provider": "renyun-self-hosted-agent",
  "conversationId": "patient_p1_session_001"
}
```

### 返回字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `answer` | string | 智能体生成的最终回答 |
| `mode` | string | 本次回答采用的运行模式 |
| `trace` | array | 本次智能体调用的工具记录 |
| `provider` | string | 固定为 `renyun-self-hosted-agent` |
| `conversationId` | string | 当前会话编号 |

### `mode` 取值

| 值 | 说明 |
|---|---|
| `model-assisted` | DeepSeek 调用成功，使用大模型组织回答 |
| `local-rules` | 未配置模型、模型余额不足或调用失败，自动使用本地规则回答 |

DeepSeek 调用失败时，系统通常不会直接向前端返回错误，而是降级为 `local-rules`，因此 HTTP 状态码仍为 `200`。

### `trace.tool` 取值

| 值 | 说明 |
|---|---|
| `safety` | 安全边界检查 |
| `training` | 训练记录汇总 |
| `tasks` | 医生任务核对 |
| `adherence` | 打卡和训练依从性汇总 |

## 5. 安全风险请求与返回

### 请求

```json
{
  "patientId": "p1",
  "question": "训练后小腿肿痛，而且现在无法负重",
  "snapshot": {
    "recentRecords": [],
    "tasks": [],
    "recentCheckins": [],
    "streakDays": 0
  }
}
```

### 返回

```json
{
  "answer": "建议你先停止当前训练，不要自行增加角度或强度。\n\n你的描述中包含需要医生确认的信号：无法负重、小腿肿痛。\n\n请联系主治医生或康复治疗师；如果症状明显、持续加重或影响正常活动，请及时就医。",
  "mode": "local-rules",
  "trace": [
    { "tool": "safety", "label": "安全边界检查", "status": "completed" },
    { "tool": "training", "label": "读取训练记录", "status": "completed" }
  ],
  "provider": "renyun-self-hosted-agent",
  "conversationId": "patient_p1_generated"
}
```

安全检查优先于大模型回答。命中风险表达时，系统可以直接使用本地规则回复，避免模型弱化风险提示。

## 6. 状态码汇总

| 状态码 | 含义 | 触发情况 |
|---|---|---|
| `200` | 请求成功 | 大模型回答成功，或成功降级为本地策略 |
| `400` | 请求参数错误 | 缺少患者编号、问题为空或问题超过 500 字符 |
| `502` | 智能体服务暂时不可用 | 自研智能体执行过程中出现未处理异常 |
| `404` | 接口不存在 | 请求地址或请求方式错误 |

## 7. 失败返回示例

### 缺少患者编号

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{ "error": "缺少患者编号" }
```

### 问题为空

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{ "error": "请输入想咨询的问题" }
```

### 问题超过 500 字符

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{ "error": "问题内容过长" }
```

### 智能体发生未处理异常

```http
HTTP/1.1 502 Bad Gateway
Content-Type: application/json
```

```json
{ "error": "康复助手暂时无法响应" }
```

## 8. 前端调用示例

```javascript
const response = await fetch('/api/patient-agent', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    patientId: 'p1',
    conversationId: 'patient_p1_session_001',
    question: '请解释我今天的训练表现',
    snapshot: {
      recentRecords: [],
      tasks: [],
      recentCheckins: [],
      streakDays: 0
    }
  })
});

const data = await response.json();

if (!response.ok) {
  throw new Error(data.error || '训练答疑服务暂时不可用');
}

console.log(data.answer);
console.log(data.mode);
console.log(data.trace);
```

## 9. 生产环境安全要求

当前比赛原型尚未实现正式的用户认证令牌。生产部署前应增加：

- 登录会话或 JWT 身份认证。
- 服务端校验患者身份和医患关系。
- 接口访问频率限制。
- 将 CORS 由 `*` 改为正式前端域名。
- 请求日志脱敏。
- 通过服务器环境变量或云端密钥管理保存 DeepSeek API Key。
- 禁止前端接触 DeepSeek API Key。
- 不向第三方模型发送姓名、手机号、身份证号等直接身份信息。

