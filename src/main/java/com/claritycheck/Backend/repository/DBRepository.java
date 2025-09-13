package com.claritycheck.Backend.repository;

import com.claritycheck.Backend.model.AgentHistory;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DBRepository extends JpaRepository<AgentHistory, Id>{
}
