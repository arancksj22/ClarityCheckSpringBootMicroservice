package com.claritycheck.Backend.service;

import com.claritycheck.Backend.model.AgentResponse;
import com.claritycheck.Backend.model.BiasReport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AggregatorService {
    private final EthicsAgent ethicsAgent;
    private final LogicalFallacyAgent logicalFallacyAgent;
    private final AssumptionAgent assumptionAgent;

    public AggregatorService(EthicsAgent ethicsAgent,
                             LogicalFallacyAgent logicalFallacyAgent,
                             AssumptionAgent assumptionAgent) {
        this.ethicsAgent = ethicsAgent;
        this.logicalFallacyAgent = logicalFallacyAgent;
        this.assumptionAgent = assumptionAgent;
    }

    public BiasReport analyzeAll(String text) {
        return new BiasReport(List.of(
                ethicsAgent.analyze(text),
                logicalFallacyAgent.analyze(text),
                assumptionAgent.analyze(text)
        ));
    }
}
