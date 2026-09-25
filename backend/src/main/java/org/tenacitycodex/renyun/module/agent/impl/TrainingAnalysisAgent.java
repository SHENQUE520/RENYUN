package org.tenacitycodex.renyun.module.agent.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.common.dto.TrainingHistoryDTO;
import org.tenacitycodex.renyun.module.agent.RehabAgent;
import org.tenacitycodex.renyun.module.user.entity.TrainingRecord;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TrainingAnalysisAgent implements RehabAgent {

    private final ChatClient chatClient;

    public TrainingAnalysisAgent(
            @Qualifier("trainingAnalysisChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "训练分析 Agent";
    }

    @Override
    public String process(AgentContext context) {
        TrainingRecord current = context.getCurrentTraining();
        List<TrainingHistoryDTO> history = context.getRecentHistory();

        String currentDesc = formatTraining(current);
        String historyDesc = history.stream()
                .map(this::formatHistory)
                .collect(Collectors.joining("\n"));

        if (historyDesc.isEmpty()) {
            historyDesc = "  暂无历史训练记录";
        }

        String prompt = String.format("""
                【本次训练数据】
                %s

                【近期历史训练记录】
                %s

                请基于本次训练数据并结合近期历史，从 Pitch（大腿俯仰角）、Roll（大腿翻滚角）、
                KED（膝关节夹角）、标准姿态占比、训练时长五个维度进行分析，
                指出本次训练的亮点与不足，并与近期表现进行对比。
                使用中文，简明专业，控制在 200 字以内。
                """, currentDesc, historyDesc);

        return callLlm(prompt);
    }

    private String callLlm(String prompt) {
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[训练分析Agent] LLM调用失败", e);
            return "训练分析服务暂不可用。";
        }
    }

    private String formatTraining(TrainingRecord r) {
        if (r == null) return "  无数据";
        return String.format(
                "  动作: %s | 日期: %s | 时长: %ss | 采样: %s点 | Pitch均/最大/最小: %s°/%s°/%s° | Roll均/最大/最小: %s°/%s°/%s° | KED均值: %s° | 标准/调整/危险: %s%%/%s%%/%s%%",
                nvl(r.getModeName()), r.getTrainingDate(),
                nvl(r.getDurationSec()), nvl(r.getSamples()),
                nvl(r.getAvgPitch()), nvl(r.getMaxPitch()), nvl(r.getMinPitch()),
                nvl(r.getAvgRoll()), nvl(r.getMaxRoll()), nvl(r.getMinRoll()),
                nvl(r.getAvgKed()),
                nvl(r.getStandardRatio()), nvl(r.getAdjustRatio()), nvl(r.getDangerRatio()));
    }

    private String formatHistory(TrainingHistoryDTO h) {
        return String.format(
                "  %s | %s | KED均值:%s° | 标准姿态:%s%% | 最大Pitch:%s°",
                h.getTrainingDate(), nvl(h.getModeName()),
                nvl(h.getAvgKed()), nvl(h.getStandardRatio()), nvl(h.getMaxPitch()));
    }

    private String nvl(Object o) {
        return o == null ? "-" : o.toString();
    }
}
