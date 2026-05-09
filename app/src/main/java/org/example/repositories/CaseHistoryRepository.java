package org.example.repositories;

import org.example.models.CaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseHistoryRepository extends JpaRepository<CaseHistory, Long> {
    List<CaseHistory> findByAssociateIdOrderByCreatedAtDesc(Long associateId);
}
