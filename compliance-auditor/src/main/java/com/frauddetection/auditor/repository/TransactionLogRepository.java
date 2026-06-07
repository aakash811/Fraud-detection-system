package com.frauddetection.auditor.repository;

import com.frauddetection.auditor.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
}