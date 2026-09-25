package org.tenacitycodex.renyun.module.agent.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.common.dto.response.PrescriptionDTO;
import org.tenacitycodex.renyun.module.agent.RehabAgent;

@Slf4j
@Component
public class DoctorAssistantAgent implements RehabAgent {

    private final ChatClient chatClient;

    public DoctorAssistantAgent(
            @Qualifier("doctorAssistantChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "医生助手 Agent";
    }

    @Override
    public String process(AgentContext context) {
        PrescriptionDTO prescription = context.getPrescription();

        String prescriptionDesc = prescription == null
                ? "  暂无处方信息"
                : String.format("  目标动作: %s | 目标ROM: %s° | 目标时长: %ss | 频次: %s次 | 备注: %s",
                        nvl(prescription.getExercise()), nvl(prescription.getTargetRom()),
                        nvl(prescription.getTargetDuration()), nvl(prescription.getFrequency()),
                        nvl(prescription.getNotes()));

        String prompt = String.format("""
                【上游 Agent 分析结果】
                —— 训练分析 ——
                %s

                —— 风险评估 ——
                %s

                —— 康复趋势 ——
                %s

                【处方目标】
                %s

                请综合以上信息，输出一份面向医生的专业摘要，包含：
                1. 患者当前恢复状态的医学专业判断
                2. 与处方目标的差距分析
                3. 下一步康复训练的任务建议（明确动作、组数/次数、注意事项）

                输出格式要求：先输出文字摘要，然后另起一行输出一个 JSON 数组（不要使用 Markdown 代码块包裹，直接以纯文本 JSON 输出），
                数组中每个元素代表一条建议的康复任务，字段为：
                {"name":"任务名称","count":数量,"unit":"单位","description":"简要说明"}

                使用中文，专业严谨，摘要控制在 300 字以内。
                """,
                safe(context.getTrainingAnalysisResult()),
                safe(context.getRiskAssessmentResult()),
                safe(context.getRehabTrendResult()),
                prescriptionDesc);

        return callLlm(prompt);
    }

    private String callLlm(String prompt) {
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[医生助手Agent] LLM调用失败", e);
            return "医生助手服务暂不可用。";
        }
    }

    private String safe(String s) {
        return s == null ? "（无）" : s;
    }

    private String nvl(Object o) {
        return o == null ? "-" : o.toString();
    }
}
