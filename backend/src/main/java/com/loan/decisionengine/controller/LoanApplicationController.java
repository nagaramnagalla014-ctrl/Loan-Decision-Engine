package com.loan.decisionengine.controller;

import com.loan.decisionengine.dto.ApiResponse;
import com.loan.decisionengine.dto.LoanApplicationRequest;
import com.loan.decisionengine.dto.ManualReviewRequest;
import com.loan.decisionengine.model.LoanApplication;
import com.loan.decisionengine.model.LoanDecision;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.service.LoanApplicationService;
import com.loan.decisionengine.service.LoanDecisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class LoanApplicationController {

    @Autowired private LoanApplicationService applicationService;
    @Autowired private LoanDecisionService decisionService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplication>> submit(
            @Valid @RequestBody LoanApplicationRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Application submitted and evaluated",
                applicationService.submit(req, user)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getMyApplications(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getMyApplications(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> getById(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getById(id, user)));
    }

    @GetMapping("/track/{applicationNumber}")
    public ResponseEntity<ApiResponse<LoanApplication>> track(@PathVariable String applicationNumber) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getByApplicationNumber(applicationNumber)));
    }

    @GetMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<LoanDecision>> getDecision(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        applicationService.getById(id, user);
        return ResponseEntity.ok(ApiResponse.ok(decisionService.getByApplicationId(id)));
    }

    @GetMapping("/manual-review")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getPendingManualReview() {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getPendingManualReview()));
    }

    @PostMapping("/{id}/manual-review")
    public ResponseEntity<ApiResponse<LoanDecision>> processManualReview(
            @PathVariable Long id,
            @Valid @RequestBody ManualReviewRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Manual review completed",
                applicationService.manualReview(id, req, user)));
    }
}
