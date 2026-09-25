package org.tenacitycodex.renyun.module.agent.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.common.dto.RomTrendDTO;
import org.tenacitycodex.renyun.module.agent.RehabAgent;

@Slf4j
@Component
public class RehabTrendAgent implements RehabAgent {

    private final ChatClient chatClient;

    public RehabTrendAgent(
            @Qualifier("rehabTrendChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String getName() {
        return "康复趋势 Agent";
    }

    @Override
    public String process(AgentContext context) {
        RomTrendDTO trend = context.getRomTrend();

        String trendDesc = formatTrend(trend);
        String trendLabel = mapTrendLabel(trend.getTrend());

        String prompt = String.format("""
                【ROM 趋势数据（近 %s 天）】
                %s

                【趋势判定】%s

                请分析患者 ROM 的变化趋势，判断康复进展是否符合预期，
                并指出需要关注的节点。使用中文，简明专业，控制在 150 字以内。
                """, trend.getDays(), trendDesc, trendLabel);

        return callLlm(prompt);
    }

    private String callLlm(String prompt) {
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[康复趋势Agent] LLM调用失败", e);
            return "康复趋势分析服务暂不可用。";
        }
    }

    private String formatTrend(RomTrendDTO trend) {
        if (trend == null || trend.getPoints() == null || trend.getPoints().isEmpty()) {
            return "  暂无趋势数据";
        }
        StringBuilder sb = new StringBuilder();
        trend.getPoints().forEach(p ->
                sb.append(String.format("  %s | ROM: %s°%n", p.getDate(), p.getRom())));
        sb.append(String.format("  起始ROM: %s° → 结束ROM: %s° | 变化: %s°",
                trend.getStartRom(), trend.getEndRom(), trend.getDeltaRom()));
        return sb.toString();
    }

    private String mapTrendLabel(String trend) {
        if (trend == null) return "数据不足";
        return switch (trend) {
            case "IMPROVING" -> "持续改善";
            case "DECLINING" -> "有所下降";
            case "STABLE" -> "基本稳定";
            default -> "数据不足";
        };
    }
}
