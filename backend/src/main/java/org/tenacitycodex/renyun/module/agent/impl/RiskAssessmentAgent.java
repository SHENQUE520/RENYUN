package org.tenacitycodex.renyun.module.agent.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.module.agent.RehabAgent;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

@Slf4j
@Component
public class RiskAssessmentAgent implements RehabAgent {

    private final ChatClient chatClient;

    public RiskAssessmentAgent(
            @Qualifier("riskAssessmentChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "风险评估 Agent";
    }

    @Override
    public String process(AgentContext context) {
        TrainingRecord current = context.getCurrentTraining();
        String knowledge = context.getKnowledgeSnippet();

        String dataDesc = formatData(current);
        String riskLevel = assessRuleBased(current);

        String prompt = String.format("""
                【本次训练数据】
                %s

                【康复知识参考】
                %s

                【初步风险等级（基于规则）】%s

                请从以下维度进行评估：
                1. KED（膝关节夹角）是否过大
                2. Pitch 最大值是否超角度
                3. 危险姿态占比（dangerRatio）是否偏高
                4. 标准姿态占比（standardRatio）是否过低
                5. Roll 异常是否明显

                请给出明确的风险等级（低风险 / 中风险 / 高风险），并说明依据与注意事项。
                使用中文，简明专业，控制在 200 字以内。
                """, dataDesc, knowledge, riskLevel);

        return callLlm(prompt);
    }

    private String callLlm(String prompt) {
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[风险评估Agent] LLM调用失败", e);
            return "风险评估服务暂不可用。";
        }
    }

    private String assessRuleBased(TrainingRecord r) {
        if (r == null) return "低风险（数据缺失）";
        int score = 0;
        if (r.getMaxPitch() != null && r.getMaxPitch() > 90) score += 2;
        else if (r.getMaxPitch() != null && r.getMaxPitch() > 75) score += 1;
        if (r.getStandardRatio() != null && r.getStandardRatio() < 50) score += 2;
        else if (r.getStandardRatio() != null && r.getStandardRatio() < 70) score += 1;
        if (r.getAvgKed() != null && r.getAvgKed() > 120) score += 2;
        if (r.getDangerRatio() != null && r.getDangerRatio() >= 10) score += 2;
        else if (r.getDangerRatio() != null && r.getDangerRatio() >= 5) score += 1;

        if (score >= 5) return "高风险";
        if (score >= 2) return "中风险";
        return "低风险";
    }

    private String formatData(TrainingRecord r) {
        if (r == null) return "  无数据";
        return String.format(
                "  动作: %s | KED均值: %s° | Pitch最大: %s° | Roll最大: %s° | 标准姿态: %s%% | 调整姿态: %s%% | 危险姿态: %s%%",
                nvl(r.getModeName()), nvl(r.getAvgKed()), nvl(r.getMaxPitch()),
                nvl(r.getMaxRoll()), nvl(r.getStandardRatio()),
                nvl(r.getAdjustRatio()), nvl(r.getDangerRatio()));
    }

    private String nvl(Object o) {
        return o == null ? "-" : o.toString();
    }
}
