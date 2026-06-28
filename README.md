# Loan Decision Engine

Rule-based loan decision engine evaluating thousands of applications daily. Business users update lending rules without any redeployment.

**Duration:** Apr 2021 – Mar 2022

## Technologies
Java 17 · Spring Boot 2.6.3 · Drools 7.73 · PostgreSQL 14 · Apache Kafka · Redis 7 · Docker · Kubernetes

## Key Features
- Drools-powered rules engine: DRL rules stored in PostgreSQL, loaded and compiled at runtime
- Zero-downtime rule updates: business users edit rules via UI; engine reloads without restart
- Kafka events broadcast decisions to downstream systems (`loan-decision-completed`, `loan-manual-review`)
- Redis caches decision results and application status
- Manual review queue for analyst override of borderline cases
- 11 seeded default rules covering credit score, DTI, employment, LTV, and interest rate adjustment

## Default Lending Rules
| Rule | Trigger | Outcome |
|---|---|---|
| Reject Poor Credit | Credit score < 580 | REJECTED |
| Reject Unemployed | UNEMPLOYED status | REJECTED |
| Reject High DTI | DTI > 50% | REJECTED |
| Reject Excessive Loan | Loan > 5× income | REJECTED |
| Mortgage LTV Check | LTV > 90% | MANUAL_REVIEW |
| Fair Credit Review | Score 580–649 | MANUAL_REVIEW |
| Elevated DTI Review | DTI 40–50% | MANUAL_REVIEW |
| Approve Good Credit | Score 650–749, DTI ≤ 40% | APPROVED @ 9.5% |
| Approve Excellent | Score ≥ 750, DTI ≤ 30% | APPROVED @ 6.5% |
| Employment Bonus | 5+ years employed | −0.5% rate |
| Self-Employed Adj | Self-employed | +1.0% rate |

## Running Locally
```bash
docker-compose up --build
```
Access at http://localhost

## Default Accounts
| Email | Password | Role |
|---|---|---|
| john.smith@loanengine.com | password123 | APPLICANT |
| analyst@loanengine.com | admin123 | ANALYST |
| rules@loanengine.com | admin123 | BUSINESS_ADMIN |
| admin@loanengine.com | admin123 | ADMIN |
