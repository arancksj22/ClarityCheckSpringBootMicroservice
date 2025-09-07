package com.claritycheck.Backend.model;

public class AgentResponse {
    private String agentName;
    private String analysis;

    public AgentResponse(String agentName, String analysis) {
        this.agentName = agentName;
        this.analysis = analysis;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAnalysis() {
        return analysis;
    }
}
