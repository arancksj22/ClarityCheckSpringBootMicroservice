package com.claritycheck.Backend.service;

import com.claritycheck.Backend.model.AgentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
public class LogicalFallacyAgent {
    private final ChatService chatService;
    private final String prompt;

    public LogicalFallacyAgent(ChatService chatService, @Value("${prompt.logic.path}") Resource promptFile) {
        this.chatService = chatService;
        try {
            this.prompt = StreamUtils.copyToString(promptFile.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read logic prompt", e);
        }
    }

    public AgentResponse analyze(String text) {
        String result = chatService.sendToGemini(prompt + "\n" + text);
        return new AgentResponse("LogicalFallacyAgent", result);
    }
}
