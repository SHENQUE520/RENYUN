package org.tenacitycodex.renyun.module.agent;

import org.tenacitycodex.renyun.common.dto.AgentContext;

public interface RehabAgent {

    String getName();

    String process(AgentContext context);
}
