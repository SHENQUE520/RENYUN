package org.tenacitycodex.renyun.module.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.dto.AgentContext;
import org.tenacitycodex.renyun.module.agent.impl.DoctorAssistantAgent;
import org.tenacitycodex.renyun.module.agent.impl.RehabTrendAgent;
import org.tenacitycodex.renyun.module.agent.impl.RiskAssessmentAgent;
import org.tenacitycodex.renyun.module.agent.impl.TrainingAnalysisAgent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoordinatorAgent {

    private final TrainingAnalysisAgent trainingAnalysisAgent;
    private final RiskAssessmentAgent riskAssessmentAgent;
    private final RehabTrendAgent rehabTrendAgent;
    private final DoctorAssistantAgent doctorAssistantAgent;

    public AgentContext executeTrainingChain(AgentContext context) {
        log.info("[Coordinator] 启动训练分析链式调用: patientId={}, trainingId={}",
                context.getPatientId(),
                context.getCurrentTraining() != null ? context.getCurrentTraining().getId() : null);

        log.info("[Coordinator] 调用 Agent A - 训练分析");
        String trainingAnalysis = trainingAnalysisAgent.process(context);
        context.setTrainingAnalysisResult(trainingAnalysis);

        log.info("[Coordinator] 调用 Agent B - 风险评估");
        String riskAssessment = riskAssessmentAgent.process(context);
        context.setRiskAssessmentResult(riskAssessment);

        log.info("[Coordinator] 调用 Agent C - 康复趋势");
        String rehabTrend = rehabTrendAgent.process(context);
        context.setRehabTrendResult(rehabTrend);

        log.info("[Coordinator] 调用 Agent D - 医生助手（综合决策）");
        String doctorSummary = doctorAssistantAgent.process(context);
        context.setDoctorSummaryResult(doctorSummary);

        log.info("[Coordinator] 链式调用完成");
        return context;
    }
}
