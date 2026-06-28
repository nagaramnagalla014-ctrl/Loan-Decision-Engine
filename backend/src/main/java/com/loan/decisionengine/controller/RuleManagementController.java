package com.loan.decisionengine.controller;

import com.loan.decisionengine.dto.ApiResponse;
import com.loan.decisionengine.dto.RuleUpdateRequest;
import com.loan.decisionengine.model.LoanRule;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.service.RuleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleManagementController {

    @Autowired private RuleManagementService ruleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanRule>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAllRules()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LoanRule>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getActiveRules()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanRule>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getRuleById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoanRule>> create(
            @Valid @RequestBody RuleUpdateRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Rule created and engine refreshed",
                ruleService.createRule(req, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanRule>> update(
            @PathVariable Long id,
            @Valid @RequestBody RuleUpdateRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Rule updated and engine refreshed",
                ruleService.updateRule(id, req, user)));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggle(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ruleService.toggleRule(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Rule toggled and engine refreshed", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ruleService.deleteRule(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Rule deleted and engine refreshed", null));
    }

    @GetMapping("/engine/version")
    public ResponseEntity<ApiResponse<Map<String, String>>> getEngineVersion() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("version", ruleService.getEngineVersion())));
    }
}
