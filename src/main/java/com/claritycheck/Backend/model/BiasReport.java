package com.claritycheck.Backend.model;

import java.util.List;

public class BiasReport {
    private List<AgentResponse> responses;

    public BiasReport(List<AgentResponse> responses) {
        this.responses = responses;
    }

    public List<AgentResponse> getResponses() {
        return responses;
    }
}
