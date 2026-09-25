# 韧小护自研智能体

这一版患者端使用团队自研的 `RehabAgent` 编排层，不依赖 AgentArts。智能体只读取现有页面已经生成的训练记录、医生任务和打卡信息，不参与 MQTT 数据处理、姿态判定、阈值计算或训练计时。

## 工作流程

1. 识别用户想了解训练记录、医生任务还是打卡情况。
2. 调用相应的本地工具读取结构化数据。
3. 在每次回答前执行安全边界检查。
4. 使用本地规则生成可演示的回答；配置模型后，则将工具结果交给模型进行自然语言组织。
5. 将本次工具调用记录返回页面，用于演示智能体的执行过程。

## 本地启动

```bash
npm install
npm start
```

访问 `http://localhost:3000/login.html`。不配置任何密钥时，智能体会使用本地策略完成全部演示流程。

## 可选的大模型增强

智能体支持 OpenAI 兼容的 Chat Completions 接口。在 `.env` 中配置：

```env
AGENT_MODEL_API_KEY=your-key
AGENT_MODEL_URL=https://api.deepseek.com/chat/completions
AGENT_MODEL_NAME=deepseek-flash
```

如果已有 `DEEPSEEK_API_KEY`，智能体会默认使用 DeepSeek 兼容接口。密钥只放在服务器环境变量中，不得写入前端。

## 华为云部署建议

- 将 Node.js 应用部署至华为云 ECS。
- 将患者、医生任务和训练记录迁移至 RDS for MySQL。
- 将康复知识文档保存在 OBS，后续再增加知识检索工具。
- 使用 CodeArts 进行代码托管、构建和部署，保留从需求到上线的过程记录。

## 医疗安全边界

- 不诊断疾病。
- 不修改医生处方、训练阈值或现有算法结果。
- 不控制支具或其他硬件。
- 出现剧痛、严重肿胀、无法负重等风险表述时，优先建议停止训练并联系医生。
