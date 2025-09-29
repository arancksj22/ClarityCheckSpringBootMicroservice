package com.claritycheck.Backend.repository;

import com.claritycheck.Backend.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {
    long countByUserId(String userId);
    List<UserDocument> findAllByUserIdOrderByCreatedAtDesc(String userId);
    Optional<UserDocument> findByIdAndUserId(UUID id, String userId);
}

