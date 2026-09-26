package org.tenacitycodex.renyun.module.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.exceptions.ApiException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.deepseek.api-key:}")
    private String apiKey;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("你是一名专业的运动康复治疗助手，擅长膝关节前交叉韧带（ACL）术后康复评估。请用中文回答，语气专业且温和。")
                .build();
    }

    public String generateReport(String mode, String modeName, Double durationSec,
                                 Integer samples, Object stats) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_deepseek")) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "服务器未配置 DEEPSEEK_API_KEY");
        }
        if (mode == null || stats == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "缺少必要的训练数据");
        }
        String statsJson = safeJson(stats);
        String prompt = String.format("""
                你是一名专业的运动康复治疗师，请根据以下训练数据生成一份康复评估报告。

                训练模式：%s
                模式名称：%s
                训练时长：%.2f 秒
                采样次数：%d

                训练数据统计：
                %s

                请输出一份结构清晰的康复评估报告，包含：
                1. 训练完成情况总结
                2. 关键指标分析（ROM、稳定性、达标率等）
                3. 潜在风险提示
                4. 下一步康复建议
                """, mode, modeName != null ? modeName : mode,
                durationSec != null ? durationSec : 0,
                samples != null ? samples : 0, statsJson);
        return chatClient.prompt().user(prompt).call().content();
    }

    public String generatePatientReport(Map<String, Object> patientData) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_deepseek")) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "服务器未配置 DEEPSEEK_API_KEY");
        }
        String dataJson = safeJson(patientData);
        String prompt = String.format("""
                你是一名专业的运动康复主治医师，请根据以下患者康复数据生成一份阶段性康复评估报告。

                患者数据：
                %s

                请输出一份完整的阶段性康复评估报告，包含：
                1. 患者基本情况概述
                2. 训练完成情况与打卡统计分析
                3. 康复进展评估
                4. 存在的问题与风险
                5. 下一阶段康复建议与目标
                """, dataJson);
        return chatClient.prompt().user(prompt).call().content();
    }

    public String chat(List<Map<String, Object>> messages, Double temperature) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_deepseek")) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "服务器未配置 DEEPSEEK_API_KEY");
        }
        if (messages == null || messages.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "缺少消息内容");
        }
        String lastUser = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).get("role"))) {
                lastUser = String.valueOf(messages.get(i).get("content"));
                break;
            }
        }
        if (lastUser.isBlank()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "缺少用户消息");
        }
        return chatClient.prompt().user(lastUser).call().content();
    }

    private String safeJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
