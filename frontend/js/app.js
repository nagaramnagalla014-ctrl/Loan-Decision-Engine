const API = '/api';
let currentUser = null;
let editingRuleId = null;
let reviewingAppId = null;

// ── Auth ──────────────────────────────────────────────────────────
function token() { return localStorage.getItem('token'); }

async function api(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (token()) opts.headers['Authorization'] = 'Bearer ' + token();
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(API + path, opts);
  if (res.status === 401) { logout(); return null; }
  return res.json();
}

async function doLogin(e) {
  e.preventDefault();
  const res = await api('POST', '/auth/login', {
    email: v('loginEmail'), password: v('loginPassword')
  });
  if (res && res.success) {
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('user', JSON.stringify(res.data));
    currentUser = res.data;
    applyAuthUI();
    showPage('home');
  } else {
    showAlert(res ? res.message : 'Login failed', 'danger');
  }
}

async function doRegister(e) {
  e.preventDefault();
  const res = await api('POST', '/auth/register', {
    firstName: v('regFirst'), lastName: v('regLast'),
    email: v('regEmail'), password: v('regPassword'), phone: v('regPhone')
  });
  if (res && res.success) {
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('user', JSON.stringify(res.data));
    currentUser = res.data;
    applyAuthUI();
    showPage('apply');
  } else {
    showAlert(res ? res.message : 'Registration failed', 'danger');
  }
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  currentUser = null;
  applyAuthUI();
  showPage('home');
}

function applyAuthUI() {
  const loggedIn = !!token();
  const role = currentUser ? currentUser.role : '';
  const isStaff = ['ANALYST','BUSINESS_ADMIN','ADMIN'].includes(role);
  const isRuleAdmin = ['BUSINESS_ADMIN','ADMIN'].includes(role);

  document.querySelectorAll('.auth-only').forEach(el => el.classList.toggle('d-none', !loggedIn));
  document.querySelectorAll('.guest-only').forEach(el => el.classList.toggle('d-none', loggedIn));
  document.querySelectorAll('.staff-only').forEach(el => el.classList.toggle('d-none', !isStaff));

  // Rules menu only for business admin / admin
  const rulesLink = document.querySelector('a[onclick*="rules"]');
  if (rulesLink) rulesLink.parentElement.classList.toggle('d-none', !isRuleAdmin);

  if (loggedIn && currentUser) {
    document.getElementById('userNav').classList.remove('d-none');
    document.getElementById('userGreeting').textContent =
      currentUser.firstName + ' (' + role + ')';
  } else {
    document.getElementById('userNav').classList.add('d-none');
  }
}

// ── Pages ─────────────────────────────────────────────────────────
function showPage(name) {
  document.querySelectorAll('[id^="page-"]').forEach(p => p.classList.add('d-none'));
  document.getElementById('page-' + name).classList.remove('d-none');
  clearAlert();
  if (name === 'track') loadMyApplications();
  if (name === 'queue') loadReviewQueue();
  if (name === 'rules') loadRules();
  if (name === 'admin') { loadAdminStats(); loadAdminTab('applications', null); }
  if (name === 'apply') prefillApplyForm();
}

function prefillApplyForm() {
  if (!currentUser) return;
  setV('appFirst', currentUser.firstName);
  setV('appLast', currentUser.lastName);
  setV('appEmail', currentUser.email);
}

// ── Loan Application ───────────────────────────────────────────────
async function submitLoan(e) {
  e.preventDefault();
  const payload = {
    firstName: v('appFirst'), lastName: v('appLast'),
    email: v('appEmail'), phone: v('appPhone'),
    loanType: v('loanType'),
    requestedAmount: parseFloat(v('requestedAmount')),
    termMonths: parseInt(v('termMonths')),
    annualIncome: parseFloat(v('annualIncome')),
    monthlyDebtPayments: parseFloat(v('monthlyDebtPayments')),
    creditScore: parseInt(v('creditScore')),
    employmentType: v('employmentType'),
    yearsAtCurrentJob: parseInt(v('yearsAtJob')) || 0,
    collateralValue: parseFloat(v('collateralValue')) || 0,
    loanPurpose: v('loanPurpose')
  };
  showAlert('Evaluating application...', 'info');
  const res = await api('POST', '/applications', payload);
  clearAlert();
  if (res && res.success) {
    renderDecisionResult(res.data);
    document.getElementById('loanForm').reset();
  } else {
    showAlert(res ? res.message : 'Submission failed', 'danger');
  }
}

function renderDecisionResult(app) {
  const d = document.getElementById('decisionResult');
  const decision = app.status;
  const color = { APPROVED: 'success', REJECTED: 'danger', MANUAL_REVIEW: 'warning', PROCESSING: 'info' }[decision] || 'secondary';
  d.innerHTML = `
    <div class="card shadow border-${color}">
      <div class="card-header bg-${color} ${decision === 'MANUAL_REVIEW' ? 'text-dark' : 'text-white'}">
        <h5 class="mb-0"><i class="fas fa-clipboard-check mr-2"></i>Decision: ${decision.replace('_', ' ')}</h5>
      </div>
      <div class="card-body">
        <p><strong>Application Number:</strong> <code>${app.applicationNumber}</code></p>
        <p><strong>Loan Type:</strong> ${app.loanType} &bull; <strong>Requested:</strong> $${fmt(app.requestedAmount)}</p>
        ${decision === 'APPROVED' ? `
          <div class="alert alert-success">
            <strong>Congratulations!</strong> Your loan has been approved.
          </div>
        ` : decision === 'REJECTED' ? `
          <div class="alert alert-danger">
            Your application did not meet our current lending criteria. You may reapply in 90 days.
          </div>
        ` : `
          <div class="alert alert-warning">
            Your application requires additional review. An analyst will assess it within 2 business days.
          </div>
        `}
        <p class="mb-1"><strong>Rules Engine:</strong> ${app.rulesEngineVersion}</p>
        <p class="mb-0 text-muted small">Track your application: <code>${app.applicationNumber}</code></p>
      </div>
    </div>`;
  d.classList.remove('d-none');
}

// ── Track ──────────────────────────────────────────────────────────
async function trackApplication(e) {
  e.preventDefault();
  const num = v('trackNumber').trim().toUpperCase();
  const res = await api('GET', '/applications/track/' + num);
  const el = document.getElementById('trackResult');
  if (res && res.success) {
    const app = res.data;
    const color = { APPROVED:'success',REJECTED:'danger',MANUAL_REVIEW:'warning',PROCESSING:'info',ERROR:'secondary' }[app.status] || 'secondary';
    el.innerHTML = `
      <div class="card shadow-sm border-${color}">
        <div class="card-header bg-${color} ${app.status==='MANUAL_REVIEW'?'text-dark':'text-white'}">
          Application ${app.applicationNumber} — ${app.status.replace('_',' ')}
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-6">
              <p><strong>Name:</strong> ${app.firstName} ${app.lastName}</p>
              <p><strong>Loan Type:</strong> ${app.loanType}</p>
              <p><strong>Requested:</strong> $${fmt(app.requestedAmount)}</p>
              <p><strong>Term:</strong> ${app.termMonths} months</p>
            </div>
            <div class="col-md-6">
              <p><strong>Submitted:</strong> ${fmtDate(app.submittedAt)}</p>
              <p><strong>Processed:</strong> ${app.processedAt ? fmtDate(app.processedAt) : 'Pending'}</p>
              <p><strong>Credit Score:</strong> ${app.creditScore}</p>
              <p><strong>DTI:</strong> ${app.debtToIncomeRatio}%</p>
            </div>
          </div>
        </div>
      </div>`;
  } else {
    el.innerHTML = `<div class="alert alert-warning">Application not found: ${num}</div>`;
  }
}

async function loadMyApplications() {
  if (!token()) return;
  const res = await api('GET', '/applications/my');
  const el = document.getElementById('myApplicationsList');
  if (!res || !res.success || !res.data.length) {
    el.innerHTML = '<p class="text-muted">No applications yet.</p>';
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover table-sm">
        <thead class="thead-light"><tr>
          <th>Application #</th><th>Type</th><th>Amount</th><th>Status</th><th>Submitted</th><th></th>
        </tr></thead>
        <tbody>${res.data.map(a => `
          <tr>
            <td><code>${a.applicationNumber}</code></td>
            <td>${a.loanType}</td>
            <td>$${fmt(a.requestedAmount)}</td>
            <td><span class="badge badge-${statusColor(a.status)}">${a.status.replace('_',' ')}</span></td>
            <td>${fmtDate(a.submittedAt)}</td>
            <td><button class="btn btn-xs btn-outline-secondary btn-sm"
                onclick="viewAppDecision(${a.id})"><i class="fas fa-eye"></i></button></td>
          </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

async function viewAppDecision(appId) {
  const res = await api('GET', '/applications/' + appId + '/decision');
  if (res && res.success) {
    const d = res.data;
    showAlert(`Decision for application: <strong>${d.decision}</strong> | Risk: ${d.riskTier} (${d.riskScore}) | Reasons: ${d.decisionReasons}`, 'info');
  }
}

// ── Review Queue ───────────────────────────────────────────────────
async function loadReviewQueue() {
  const res = await api('GET', '/applications/manual-review');
  const el = document.getElementById('reviewQueue');
  if (!res || !res.success || !res.data.length) {
    el.innerHTML = '<div class="alert alert-success"><i class="fas fa-check-circle mr-2"></i>No applications pending manual review.</div>';
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover">
        <thead class="thead-light"><tr>
          <th>Application #</th><th>Name</th><th>Type</th><th>Amount</th>
          <th>Credit Score</th><th>DTI %</th><th>Submitted</th><th>Action</th>
        </tr></thead>
        <tbody>${res.data.map(a => `
          <tr>
            <td><code>${a.applicationNumber}</code></td>
            <td>${a.firstName} ${a.lastName}</td>
            <td>${a.loanType}</td>
            <td>$${fmt(a.requestedAmount)}</td>
            <td>${a.creditScore}</td>
            <td>${a.debtToIncomeRatio}%</td>
            <td>${fmtDate(a.submittedAt)}</td>
            <td><button class="btn btn-warning btn-sm" onclick="openReview(${a.id},'${a.applicationNumber}',${a.requestedAmount})">
              <i class="fas fa-gavel mr-1"></i>Review</button></td>
          </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

function openReview(appId, appNumber, amount) {
  reviewingAppId = appId;
  document.getElementById('reviewAppNumber').textContent = appNumber;
  setV('reviewAmount', amount);
  setV('reviewRate', '');
  setV('reviewNotes', '');
  document.getElementById('reviewModal').classList.remove('d-none');
}

function cancelReview() {
  reviewingAppId = null;
  document.getElementById('reviewModal').classList.add('d-none');
}

async function submitReview() {
  const payload = {
    decision: v('reviewDecision'),
    approvedAmount: parseFloat(v('reviewAmount')) || null,
    interestRate: parseFloat(v('reviewRate')) || null,
    reviewNotes: v('reviewNotes')
  };
  const res = await api('POST', '/applications/' + reviewingAppId + '/manual-review', payload);
  if (res && res.success) {
    showAlert('Review submitted: ' + res.data.decision, 'success');
    cancelReview();
    loadReviewQueue();
  } else {
    showAlert(res ? res.message : 'Review failed', 'danger');
  }
}

// ── Rules ──────────────────────────────────────────────────────────
async function loadRules() {
  const [rRes, vRes] = await Promise.all([
    api('GET', '/rules'),
    api('GET', '/rules/engine/version')
  ]);
  if (vRes && vRes.success) {
    document.getElementById('engineVersionBadge').textContent = 'Engine: ' + vRes.data.version;
  }
  const el = document.getElementById('rulesTable');
  if (!rRes || !rRes.success || !rRes.data.length) {
    el.innerHTML = '<p class="text-muted">No rules defined.</p>';
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-bordered table-hover">
        <thead class="thead-dark"><tr>
          <th>Name</th><th>Category</th><th>Description</th><th>Salience</th>
          <th>Version</th><th>Active</th><th>Actions</th>
        </tr></thead>
        <tbody>${rRes.data.map(r => `
          <tr class="rule-row ${r.active ? 'rule-active' : 'rule-inactive'}">
            <td><code>${r.ruleName}</code></td>
            <td><span class="badge badge-secondary">${r.category}</span></td>
            <td>${r.ruleDescription}</td>
            <td>${r.salience}</td>
            <td>${r.version}</td>
            <td><span class="badge badge-${r.active ? 'success' : 'secondary'}">${r.active ? 'Active' : 'Inactive'}</span></td>
            <td>
              <button class="btn btn-xs btn-outline-primary btn-sm mr-1" onclick="editRule(${r.id})"><i class="fas fa-edit"></i></button>
              <button class="btn btn-xs btn-outline-warning btn-sm mr-1" onclick="toggleRule(${r.id})"><i class="fas fa-toggle-on"></i></button>
              <button class="btn btn-xs btn-outline-danger btn-sm" onclick="deleteRule(${r.id},'${r.ruleName}')"><i class="fas fa-trash"></i></button>
            </td>
          </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

function showRuleForm(rule) {
  editingRuleId = rule ? rule.id : null;
  document.getElementById('ruleFormTitle').textContent = rule ? 'Edit Rule' : 'New Rule';
  setV('rfName', rule ? rule.ruleName : '');
  setV('rfDesc', rule ? rule.ruleDescription : '');
  setV('rfSalience', rule ? rule.salience : 50);
  if (rule) document.getElementById('rfCategory').value = rule.category;
  setV('rfDrl', rule ? rule.drlContent : defaultDrl());
  document.getElementById('rfActive').checked = rule ? rule.active : true;
  document.getElementById('ruleForm').classList.remove('d-none');
  document.getElementById('ruleForm').scrollIntoView({ behavior: 'smooth' });
}

function defaultDrl() {
  return `package com.loan.decisionengine.rules;

import com.loan.decisionengine.drools.facts.LoanApplicationFact;
import com.loan.decisionengine.drools.facts.DecisionResult;

rule "My Rule Name"
    salience 50
    when
        $fact : LoanApplicationFact( creditScore < 600 )
        $result : DecisionResult( decision == null || decision.isEmpty() )
    then
        $result.setDecision("REJECTED");
        $result.setRiskTier("HIGH");
        $result.setRiskScore(75.0);
        $result.addReason("Custom rejection reason here");
end`;
}

async function editRule(id) {
  const res = await api('GET', '/rules/' + id);
  if (res && res.success) showRuleForm(res.data);
}

async function saveRule() {
  const payload = {
    ruleName: v('rfName'),
    ruleDescription: v('rfDesc'),
    category: v('rfCategory') || document.getElementById('rfCategory').value,
    drlContent: v('rfDrl'),
    salience: parseInt(v('rfSalience')),
    active: document.getElementById('rfActive').checked
  };
  const res = editingRuleId
    ? await api('PUT', '/rules/' + editingRuleId, payload)
    : await api('POST', '/rules', payload);
  if (res && res.success) {
    showAlert('Rule saved and engine refreshed successfully', 'success');
    cancelRuleForm();
    loadRules();
  } else {
    showAlert(res ? res.message : 'Save failed', 'danger');
  }
}

async function toggleRule(id) {
  const res = await api('POST', '/rules/' + id + '/toggle');
  if (res && res.success) { showAlert('Rule toggled', 'success'); loadRules(); }
  else showAlert(res ? res.message : 'Toggle failed', 'danger');
}

async function deleteRule(id, name) {
  if (!confirm('Delete rule "' + name + '"? This will immediately remove it from the engine.')) return;
  const res = await api('DELETE', '/rules/' + id);
  if (res && res.success) { showAlert('Rule deleted', 'success'); loadRules(); }
  else showAlert(res ? res.message : 'Delete failed', 'danger');
}

function cancelRuleForm() {
  editingRuleId = null;
  document.getElementById('ruleForm').classList.add('d-none');
}

function v(id) { return (document.getElementById(id) || {}).value || ''; }
function setV(id, val) { const el = document.getElementById(id); if (el) el.value = val; }

// ── Admin Dashboard ────────────────────────────────────────────────
async function loadAdminStats() {
  const res = await api('GET', '/admin/dashboard');
  if (!res || !res.success) return;
  const s = res.data;
  const cards = [
    { label:'Total Applications', value: s.totalApplications, color:'blue', icon:'file-alt' },
    { label:'Approved', value: s.approved, color:'green', icon:'check-circle' },
    { label:'Rejected', value: s.rejected, color:'red', icon:'times-circle' },
    { label:'Manual Review', value: s.manualReview, color:'yellow', icon:'user-clock' },
    { label:'Total Users', value: s.totalUsers, color:'teal', icon:'users' },
    { label:'Active Rules', value: s.activeRules, color:'purple', icon:'sliders-h' },
  ];
  document.getElementById('adminStats').innerHTML = cards.map(c => `
    <div class="col-md-2 col-6 mb-3">
      <div class="card shadow-sm stat-card ${c.color} p-3">
        <div class="text-muted small"><i class="fas fa-${c.icon} mr-1"></i>${c.label}</div>
        <div class="h3 mb-0 font-weight-bold">${c.value}</div>
      </div>
    </div>`).join('');
}

async function loadAdminTab(tab, el) {
  document.querySelectorAll('#adminTabs .nav-link').forEach(l => l.classList.remove('active'));
  if (el) el.classList.add('active');
  const container = document.getElementById('adminTabContent');

  if (tab === 'applications') {
    const res = await api('GET', '/admin/applications');
    if (!res || !res.success) return;
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover table-sm">
          <thead class="thead-light"><tr>
            <th>App #</th><th>Name</th><th>Type</th><th>Amount</th>
            <th>Credit</th><th>DTI</th><th>Status</th><th>Submitted</th>
          </tr></thead>
          <tbody>${res.data.map(a => `
            <tr>
              <td><code>${a.applicationNumber}</code></td>
              <td>${a.firstName} ${a.lastName}</td>
              <td>${a.loanType}</td>
              <td>$${fmt(a.requestedAmount)}</td>
              <td>${a.creditScore}</td>
              <td>${a.debtToIncomeRatio}%</td>
              <td><span class="badge badge-${statusColor(a.status)}">${a.status.replace('_',' ')}</span></td>
              <td>${fmtDate(a.submittedAt)}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;

  } else if (tab === 'users') {
    const res = await api('GET', '/admin/users');
    if (!res || !res.success) return;
    container.innerHTML = `
      <div class="table-responsive">
        <table class="table table-hover table-sm">
          <thead class="thead-light"><tr>
            <th>Name</th><th>Email</th><th>Role</th><th>Active</th><th>Action</th>
          </tr></thead>
          <tbody>${res.data.map(u => `
            <tr>
              <td>${u.firstName} ${u.lastName}</td>
              <td>${u.email}</td>
              <td><span class="badge badge-secondary">${u.role}</span></td>
              <td><span class="badge badge-${u.active ? 'success' : 'danger'}">${u.active ? 'Active' : 'Inactive'}</span></td>
              <td><button class="btn btn-sm btn-outline-secondary" onclick="toggleUser(${u.id})">Toggle</button></td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  }
}

async function toggleUser(id) {
  await api('PUT', '/admin/users/' + id + '/toggle');
  loadAdminTab('users', null);
}

// ── Helpers ────────────────────────────────────────────────────────
function fmt(n) { return n ? parseFloat(n).toLocaleString('en-US', {minimumFractionDigits:2,maximumFractionDigits:2}) : '0.00'; }
function fmtDate(d) { return d ? new Date(d).toLocaleDateString('en-US', {year:'numeric',month:'short',day:'numeric'}) : '—'; }
function statusColor(s) {
  return { APPROVED:'success', REJECTED:'danger', MANUAL_REVIEW:'warning', PROCESSING:'info', ERROR:'secondary' }[s] || 'secondary';
}
function showAlert(msg, type) {
  const b = document.getElementById('alertBanner');
  b.className = 'alert alert-' + type + ' mb-0 rounded-0';
  b.innerHTML = msg;
  b.classList.remove('d-none');
}
function clearAlert() { document.getElementById('alertBanner').classList.add('d-none'); }

// ── Init ───────────────────────────────────────────────────────────
(function init() {
  const stored = localStorage.getItem('user');
  if (stored) {
    try { currentUser = JSON.parse(stored); } catch(e) {}
  }
  applyAuthUI();
  showPage('home');
})();
