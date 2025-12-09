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
        try {
            // 1. Call Ethics Agent
            AgentResponse ethics = ethicsAgent.analyze(text);

            // 🛑 SAFETY PAUSE: Wait 2 seconds so we don't hit the 429 Limit
            Thread.sleep(2000);

            // 2. Call Logic Agent
            AgentResponse logic = logicalFallacyAgent.analyze(text);

            // 🛑 SAFETY PAUSE: Wait 2 seconds
            Thread.sleep(2000);

            // 3. Call Assumption Agent
            AgentResponse assumption = assumptionAgent.analyze(text);

            // 4. Return Report
            return new BiasReport(List.of(ethics, logic, assumption));

        } catch (InterruptedException e) {
            // Restore interrupted state
            Thread.currentThread().interrupt();
            throw new RuntimeException("Analysis was interrupted.");
        } catch (Exception e) {
            // If the API fails even after waiting, log it but don't crash the whole app
            System.err.println("⚠️ Analysis failed: " + e.getMessage());
            // You might want to throw a custom exception or return a partial report here
            throw new RuntimeException("Gemini API overloaded. Please try again in 1 minute.");
        }
    }
}