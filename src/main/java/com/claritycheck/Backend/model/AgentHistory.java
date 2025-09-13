package com.claritycheck.Backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AgentHistory {

    @Id
    private int id;

    private String assumptionHistoryString;
    private String biasHistoryString;
    private String logicalHistoryString;

}
