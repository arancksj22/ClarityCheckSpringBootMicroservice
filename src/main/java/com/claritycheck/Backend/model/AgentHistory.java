package com.claritycheck.Backend.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "agent_history")
public class AgentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 190)
    private String userId; // from JWT (sub) when available

    @Column
    private UUID documentId; // FK to UserDocument.id (nullable)

    @Lob
    private String assumptionHistoryString;

    @Lob
    private String biasHistoryString;

    @Lob
    private String logicalHistoryString;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public String getAssumptionHistoryString() { return assumptionHistoryString; }
    public void setAssumptionHistoryString(String assumptionHistoryString) { this.assumptionHistoryString = assumptionHistoryString; }
    public String getBiasHistoryString() { return biasHistoryString; }
    public void setBiasHistoryString(String biasHistoryString) { this.biasHistoryString = biasHistoryString; }
    public String getLogicalHistoryString() { return logicalHistoryString; }
    public void setLogicalHistoryString(String logicalHistoryString) { this.logicalHistoryString = logicalHistoryString; }
}
