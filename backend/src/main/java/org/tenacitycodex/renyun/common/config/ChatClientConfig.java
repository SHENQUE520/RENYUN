package org.tenacitycodex.renyun.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为多 Agent 链式调用中的每个 Agent 配置独立的 ChatClient。
 * 每个 ChatClient 通过 {@code defaultSystem} 预设该 Agent 的角色与行为约束，
 * 由各 Agent 通过 {@code @Qualifier} 按名称注入。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient trainingAnalysisChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一名专业的运动康复治疗师，擅长根据 ACL（前交叉韧带）术后康复训练数据进行训练质量分析。
                        你需要从 ROM（关节活动度）、KED（膝关节屈曲代偿）、达标率、稳定性、训练时长五个维度进行分析，
                        指出本次训练的亮点与不足，并与近期表现进行对比。
                        使用中文回答，语气专业且温和，输出简明扼要。
                        """)
                .build();
    }

    @Bean
    public ChatClient riskAssessmentChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一名专业的运动康复风险评估师，擅长识别 ACL 术后康复训练中的潜在风险。
                        你需要从 KED（膝关节屈曲代偿）、动作稳定性、超角度风险、异常/风险事件次数、设备数据异常五个维度进行评估，
                        给出明确的风险等级（低风险 / 中风险 / 高风险），并说明依据与注意事项。
                        使用中文回答，语气专业严谨。
                        """)
                .build();
    }

    @Bean
    public ChatClient rehabTrendChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一名专业的运动康复数据分析师，擅长解读 ACL 术后康复训练的长期趋势。
                        你需要分析 ROM（关节活动度）随时间的变化，判断康复进展是否符合预期，
                        并指出需要关注的关键节点。
                        使用中文回答，语气客观专业。
                        """)
                .build();
    }

    @Bean
    public ChatClient doctorAssistantChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一名资深的运动康复主治医师，负责为 ACL 术后患者制定康复决策与任务建议。
                        你需要综合训练分析、风险评估、康复趋势与处方目标，输出面向医生的专业摘要，
                        并给出下一步康复训练任务建议（明确动作、组数/次数、注意事项）。
                        使用中文回答，专业严谨，鼓励性适度。
                        """)
                .build();
    }
}
