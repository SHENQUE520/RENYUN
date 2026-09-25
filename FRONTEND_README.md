# 韧云智护·训练数据解读智能体

面向膝关节居家康复患者的训练监测与数据解读原型。项目在不改变现有姿态判定、训练计时和风险阈值逻辑的前提下，增加了团队自研的患者端 `RehabAgent` 编排层。

## 主要功能

- 训练动作与传感器数据监测
- 医生任务和患者打卡管理
- 自研智能体的意图规划、工具调用和短期记忆
- DeepSeek API 语言能力增强
- 医疗安全边界检查和无模型降级策略
- 医生端、患者端和康复训练报告

## 本地运行

1. 安装 Node.js 18 或更高版本。
2. 安装依赖：

   ```bash
   npm install
   ```

3. 复制环境变量示例：

   ```bash
   cp .env.example .env
   ```

4. 在 `.env` 中填入自己的 DeepSeek API Key。
5. 启动服务：

   ```bash
   npm start
   ```

6. 打开 `http://localhost:3000/login.html`。

演示患者账号为 `zhangsan` / `123456`，医生账号为 `doctor` / `123456`。

## 测试

```bash
npm test
```

## 密钥安全

- `.env` 已加入 `.gitignore`，不会被提交到 GitHub。
- 前端代码不包含 DeepSeek API Key。
- 线上部署时应在服务器或云平台的密钥管理中配置环境变量。
- 如果密钥曾被提交、截图或发送给他人，应立即在 DeepSeek 控制台撤销并新建密钥。

更详细的智能体和华为云部署说明见 [SELF_HOSTED_AGENT.md](./SELF_HOSTED_AGENT.md)。

患者端智能体的请求参数、返回结构、状态码和失败示例见 [患者端智能体 API 文档](./docs/PATIENT_AGENT_API.md)。

## 重要说明

本项目仅用于训练数据说明和竞赛演示，不提供疾病诊断，不修改医生处方，不直接控制康复支具。
