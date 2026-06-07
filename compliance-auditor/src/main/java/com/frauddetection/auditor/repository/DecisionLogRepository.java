package com.frauddetection.auditor.repository;

import com.frauddetection.auditor.entity.DecisionLog;
import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.InvestigationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, UUID> {
    List<DecisionLog> findByCardLast4OrderByOccurredAtDesc(String cardLast4);
    List<DecisionLog> findByAction(Action action);
    List<DecisionLog> findByOccurredAtBetween(Instant start, Instant end);
    List<DecisionLog> findByInvestigationStatus(InvestigationStatus status);
    
    @Query("SELECT d.action, COUNT(d) FROM DecisionLog d GROUP BY d.action")
    List<Object[]> countByAction();
}