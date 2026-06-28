package com.loan.decisionengine.drools;

import com.loan.decisionengine.drools.facts.DecisionResult;
import com.loan.decisionengine.drools.facts.LoanApplicationFact;
import com.loan.decisionengine.exception.LoanDecisionException;
import com.loan.decisionengine.model.LoanRule;
import com.loan.decisionengine.repository.LoanRuleRepository;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DroolsRuleService {

    private static final Logger log = LoggerFactory.getLogger(DroolsRuleService.class);

    @Autowired
    private LoanRuleRepository ruleRepository;

    private final AtomicReference<KieContainer> containerRef = new AtomicReference<>();
    private volatile String currentEngineVersion = "1.0";

    @PostConstruct
    public void init() {
        refreshContainer();
    }

    public synchronized void refreshContainer() {
        try {
            log.info("Building Drools KieContainer from {} active rules",
                    ruleRepository.findByActiveTrueOrderBySalienceDesc().size());

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();

            KieModuleModel model = ks.newKieModuleModel();
            KieBaseModel kieBaseModel = model.newKieBaseModel("LoanDecisionBase")
                    .setDefault(true)
                    .setEventProcessingMode(EventProcessingOption.CLOUD);
            kieBaseModel.newKieSessionModel("LoanDecisionSession")
                    .setDefault(true)
                    .setType(KieSessionModel.KieSessionType.STATELESS);

            kfs.writeKModuleXML(model.toXML());

            List<LoanRule> activeRules = ruleRepository.findByActiveTrueOrderBySalienceDesc();
            for (LoanRule rule : activeRules) {
                String safeName = rule.getRuleName().replaceAll("[^a-zA-Z0-9_]", "_");
                kfs.write("src/main/resources/rules/" + safeName + ".drl", rule.getDrlContent());
            }

            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                throw new LoanDecisionException("Drools compilation errors: " +
                        kieBuilder.getResults().getMessages());
            }

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = ks.newKieContainer(kieModule.getReleaseId());

            KieContainer old = containerRef.getAndSet(newContainer);
            if (old != null) {
                try { old.dispose(); } catch (Exception ignored) {}
            }

            currentEngineVersion = "1.0." + activeRules.size();
            log.info("Drools engine refreshed with {} rules. Version: {}", activeRules.size(), currentEngineVersion);

        } catch (LoanDecisionException e) {
            throw e;
        } catch (Exception e) {
            throw new LoanDecisionException("Failed to initialize Drools engine: " + e.getMessage());
        }
    }

    public DecisionResult evaluate(LoanApplicationFact fact) {
        KieContainer container = containerRef.get();
        if (container == null) {
            throw new LoanDecisionException("Drools rules engine is not initialized");
        }

        DecisionResult result = new DecisionResult();
        StatelessKieSession session = container.newStatelessKieSession("LoanDecisionSession");

        List<Object> facts = new ArrayList<>(Arrays.asList(fact, result));
        session.execute(facts);

        // Default outcome if no rules fired
        if (!result.isDecided()) {
            result.setDecision("MANUAL_REVIEW");
            result.setRiskTier("MEDIUM");
            result.setRiskScore(50.0);
            result.addReason("No definitive rule matched — routed to manual review");
        }

        return result;
    }

    public String getCurrentEngineVersion() {
        return currentEngineVersion;
    }
}
