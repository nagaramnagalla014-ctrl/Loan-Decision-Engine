package com.loan.decisionengine.repository;

import com.loan.decisionengine.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);
    List<LoanApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    List<LoanApplication> findByStatusOrderByCreatedAtDesc(LoanApplication.ApplicationStatus status);
    List<LoanApplication> findAllByOrderByCreatedAtDesc();
    long countByStatus(LoanApplication.ApplicationStatus status);
    @Query("SELECT l FROM LoanApplication l WHERE l.status = 'MANUAL_REVIEW' ORDER BY l.createdAt ASC")
    List<LoanApplication> findPendingManualReview();
}
