package com.claritycheck.Backend.controller;

import com.claritycheck.Backend.model.ChatRequest;
import com.claritycheck.Backend.model.BiasReport;
import com.claritycheck.Backend.service.AggregatorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final AggregatorService aggregatorService;

    public ChatController(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @PostMapping
    public BiasReport analyzePaper(@RequestBody ChatRequest request) {
        return aggregatorService.analyzeAll(request.getText());
    }
}
