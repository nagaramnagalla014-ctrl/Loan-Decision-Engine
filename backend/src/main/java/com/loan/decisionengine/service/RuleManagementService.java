package com.loan.decisionengine.service;

import com.loan.decisionengine.drools.DroolsRuleService;
import com.loan.decisionengine.dto.RuleUpdateRequest;
import com.loan.decisionengine.exception.LoanDecisionException;
import com.loan.decisionengine.kafka.LoanEventProducer;
import com.loan.decisionengine.kafka.RuleUpdateEvent;
import com.loan.decisionengine.model.LoanRule;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.repository.LoanRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RuleManagementService {

    @Autowired private LoanRuleRepository ruleRepository;
    @Autowired private DroolsRuleService droolsRuleService;
    @Autowired private LoanEventProducer eventProducer;

    public List<LoanRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public List<LoanRule> getActiveRules() {
        return ruleRepository.findByActiveTrueOrderBySalienceDesc();
    }

    public LoanRule getRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new LoanDecisionException("Rule not found: " + id));
    }

    @Transactional
    public LoanRule createRule(RuleUpdateRequest req, User actor) {
        if (ruleRepository.existsByRuleName(req.getRuleName())) {
            throw new LoanDecisionException("Rule with name '" + req.getRuleName() + "' already exists");
        }
        LoanRule rule = new LoanRule();
        rule.setRuleName(req.getRuleName());
        rule.setRuleDescription(req.getRuleDescription());
        rule.setCategory(LoanRule.RuleCategory.valueOf(req.getCategory()));
        rule.setDrlContent(req.getDrlContent());
        rule.setSalience(req.getSalience());
        rule.setActive(req.getActive() != null ? req.getActive() : true);
        rule.setCreatedBy(actor.getEmail());
        rule.setLastModifiedBy(actor.getEmail());
        LoanRule saved = ruleRepository.save(rule);

        refreshEngineAndNotify(saved, "CREATED", actor.getEmail());
        return saved;
    }

    @Transactional
    public LoanRule updateRule(Long id, RuleUpdateRequest req, User actor) {
        LoanRule rule = getRuleById(id);
        if (!rule.getRuleName().equals(req.getRuleName()) &&
                ruleRepository.existsByRuleName(req.getRuleName())) {
            throw new LoanDecisionException("Rule name '" + req.getRuleName() + "' is already taken");
        }
        rule.setRuleName(req.getRuleName());
        rule.setRuleDescription(req.getRuleDescription());
        rule.setCategory(LoanRule.RuleCategory.valueOf(req.getCategory()));
        rule.setDrlContent(req.getDrlContent());
        rule.setSalience(req.getSalience());
        if (req.getActive() != null) rule.setActive(req.getActive());
        rule.setLastModifiedBy(actor.getEmail());
        LoanRule saved = ruleRepository.save(rule);

        refreshEngineAndNotify(saved, "UPDATED", actor.getEmail());
        return saved;
    }

    @Transactional
    public void toggleRule(Long id, User actor) {
        LoanRule rule = getRuleById(id);
        rule.setActive(!rule.isActive());
        rule.setLastModifiedBy(actor.getEmail());
        LoanRule saved = ruleRepository.save(rule);

        refreshEngineAndNotify(saved, rule.isActive() ? "ACTIVATED" : "DEACTIVATED", actor.getEmail());
    }

    @Transactional
    public void deleteRule(Long id, User actor) {
        LoanRule rule = getRuleById(id);
        ruleRepository.delete(rule);
        refreshEngineAndNotify(rule, "DELETED", actor.getEmail());
    }

    private void refreshEngineAndNotify(LoanRule rule, String action, String actorEmail) {
        droolsRuleService.refreshContainer();
        eventProducer.sendRuleUpdate(new RuleUpdateEvent(
                action, rule.getId(), rule.getRuleName(),
                actorEmail, droolsRuleService.getCurrentEngineVersion()));
    }

    public String getEngineVersion() {
        return droolsRuleService.getCurrentEngineVersion();
    }
}
