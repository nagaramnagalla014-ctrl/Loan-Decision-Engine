package com.loan.decisionengine.controller;

import com.loan.decisionengine.dto.ApiResponse;
import com.loan.decisionengine.model.LoanApplication;
import com.loan.decisionengine.model.LoanDecision;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.repository.LoanApplicationRepository;
import com.loan.decisionengine.repository.LoanDecisionRepository;
import com.loan.decisionengine.repository.UserRepository;
import com.loan.decisionengine.service.RuleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private LoanApplicationRepository applicationRepository;
    @Autowired private LoanDecisionRepository decisionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RuleManagementService ruleService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", applicationRepository.count());
        stats.put("approved", applicationRepository.countByStatus(LoanApplication.ApplicationStatus.APPROVED));
        stats.put("rejected", applicationRepository.countByStatus(LoanApplication.ApplicationStatus.REJECTED));
        stats.put("manualReview", applicationRepository.countByStatus(LoanApplication.ApplicationStatus.MANUAL_REVIEW));
        stats.put("processing", applicationRepository.countByStatus(LoanApplication.ApplicationStatus.PROCESSING));
        stats.put("totalUsers", userRepository.count());
        stats.put("activeRules", ruleService.getActiveRules().size());
        stats.put("engineVersion", ruleService.getEngineVersion());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getAllApplications(
            @RequestParam(required = false) String status) {
        List<LoanApplication> apps = status != null && !status.isEmpty()
                ? applicationRepository.findByStatusOrderByCreatedAtDesc(
                        LoanApplication.ApplicationStatus.valueOf(status.toUpperCase()))
                : applicationRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.ok(apps));
    }

    @GetMapping("/decisions")
    public ResponseEntity<ApiResponse<List<LoanDecision>>> getAllDecisions() {
        return ResponseEntity.ok(ApiResponse.ok(decisionRepository.findAll()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(required = false) String role) {
        List<User> users = role != null && !role.isEmpty()
                ? userRepository.findByRole(User.Role.valueOf(role.toUpperCase()))
                : userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(!u.isActive());
            userRepository.save(u);
        });
        return ResponseEntity.ok(ApiResponse.ok("User status toggled", null));
    }
}
