// ============================================
// 签到管理系统 - 主应用逻辑
// ============================================

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    const token = API.getToken();
    if (token) {
        showApp();
        navigateTo('dashboard');
    }
    updateClock();
    setInterval(updateClock, 1000);
});

function updateClock() {
    const el = document.getElementById('currentTime');
    if (el) el.textContent = new Date().toLocaleString('zh-CN');
}

// ========== 登录/登出 ==========

async function handleLogin(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    btn.disabled = true; btn.textContent = '登录中...';
    document.getElementById('loginError').textContent = '';
    const res = await API.login(
        document.getElementById('username').value,
        document.getElementById('password').value
    );
    btn.disabled = false; btn.textContent = '登 录';
    if (res.success) {
        currentUser = res.data;
        showApp();
        navigateTo('dashboard');
    } else {
        document.getElementById('loginError').textContent = res.message || '登录失败';
    }
}

function handleLogout() {
    API.clearToken();
    currentUser = null;
    document.getElementById('app').style.display = 'none';
    document.getElementById('loginPage').style.display = 'flex';
}

function showApp() {
    document.getElementById('loginPage').style.display = 'none';
    document.getElementById('app').style.display = 'flex';
    if (currentUser) {
        document.getElementById('userName').textContent = currentUser.realName || currentUser.username;
        document.getElementById('userRole').textContent = currentUser.role;
        document.getElementById('userAvatar').textContent = (currentUser.realName || currentUser.username).charAt(0);
    }
}

// ========== 导航 ==========

document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
        item.classList.add('active');
        navigateTo(item.dataset.page);
    });
});

function navigateTo(page) {
    const titles = {
        dashboard: '📊 工作台', departments: '🏢 部门管理', employees: '👥 员工管理',
        'salary-items': '📋 工资项目定义', payroll: '🧮 工资核算', payslips: '📄 工资条',
        reports: '📈 统计报表', system: '⚙️ 系统设置'
    };
    document.getElementById('pageTitle').textContent = titles[page] || '工作台';
    switch (page) {
        case 'dashboard': renderDashboard(); break;
        case 'departments': renderDepartments(); break;
        case 'employees': renderEmployees(); break;
        case 'salary-items': renderSalaryItems(); break;
        case 'payroll': renderPayroll(); break;
        case 'payslips': renderPayslips(); break;
        case 'reports': renderReports(); break;
        case 'system': renderSystem(); break;
        default: renderDashboard();
    }
}

// ========== 工具函数 ==========

function showModal(title, bodyHtml, footerHtml) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">✕</button>
            </div>
            <div class="modal-body">${bodyHtml}</div>
            ${footerHtml ? '<div class="modal-footer">' + footerHtml + '</div>' : ''}
        </div>`;
    overlay.addEventListener('click', (e) => { if (e.target === overlay) overlay.remove(); });
    document.body.appendChild(overlay);
    return overlay;
}

function closeModal() {
    const m = document.querySelector('.modal-overlay');
    if (m) m.remove();
}

function showToast(msg, type) {
    const el = document.createElement('div');
    el.style.cssText = `position:fixed;top:20px;right:20px;padding:12px 20px;border-radius:10px;font-size:14px;font-weight:500;z-index:2000;animation:slideUp 0.3s;background:${type==='error'?'#fef2f2':'#ecfdf5'};color:${type==='error'?'#dc2626':'#059669'};border:1px solid ${type==='error'?'#fecaca':'#a7f3d0'};box-shadow:0 4px 12px rgba(0,0,0,0.1);`;
    el.textContent = msg;
    document.body.appendChild(el);
    setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity 0.3s'; setTimeout(() => el.remove(), 300); }, 3000);
}

// ========== 页面渲染函数 ==========

// ---- Dashboard ----
async function renderDashboard() {
    const el = document.getElementById('pageContent');
    el.innerHTML = `<div class="loading"><div class="spinner"></div><span class="loading-text">加载中...</span></div>`;
    try {
        const [deptRes, empRes, itemRes] = await Promise.all([
            API.getActiveDepartments(), API.getAllEmployees(), API.getSalaryItems()
        ]);
        const now = new Date();
        const ym = now.getFullYear() + String(now.getMonth() + 1).padStart(2, '0');
        const payrollRes = await API.getPayrollRecords(ym);
        const records = payrollRes.success && payrollRes.data ? payrollRes.data : [];
        el.innerHTML = `
            <div class="stats-grid">
                <div class="stat-card blue"><div class="stat-icon">🏢</div><div class="stat-info"><h3>${deptRes.success ? deptRes.data.length : 0}</h3><p>部门数量</p></div></div>
                <div class="stat-card green"><div class="stat-icon">👥</div><div class="stat-info"><h3>${empRes.success ? empRes.data.length : 0}</h3><p>在职员工</p></div></div>
                <div class="stat-card yellow"><div class="stat-icon">📋</div><div class="stat-info"><h3>${itemRes.success ? itemRes.data.length : 0}</h3><p>工资项目</p></div></div>
                <div class="stat-card red"><div class="stat-icon">💰</div><div class="stat-info"><h3>${records.length}</h3><p>本月已核算</p></div></div>
            </div>
            <div class="card">
                <div class="card-header"><h3>快捷操作</h3></div>
                <div class="card-body">
                    <div class="quick-actions">
                        <div class="quick-action-card" onclick="navigateTo('employees')"><div class="qa-icon">👤</div><div class="qa-title">员工管理</div><div class="qa-desc">增删改查员工档案</div></div>
                        <div class="quick-action-card" onclick="navigateTo('salary-items')"><div class="qa-icon">📋</div><div class="qa-title">工资项目</div><div class="qa-desc">自定义工资项公式</div></div>
                        <div class="quick-action-card" onclick="navigateTo('payroll')"><div class="qa-icon">🧮</div><div class="qa-title">工资计算</div><div class="qa-desc">一键核算全员工资</div></div>
                        <div class="quick-action-card" onclick="navigateTo('reports')"><div class="qa-icon">📈</div><div class="qa-title">报表分析</div><div class="qa-desc">部门汇总、个税报表</div></div>
                    </div>
                </div>
            </div>
            <div class="card" style="margin-top:20px;">
                <div class="card-header"><h3>📌 使用说明</h3></div>
                <div class="card-body" style="font-size:14px;color:var(--gray-600);line-height:2;">
                    <p>1️⃣ 首先在 <b>部门管理</b> 中维护组织架构</p>
                    <p>2️⃣ 然后在 <b>员工管理</b> 中添加员工信息</p>
                    <p>3️⃣ 在 <b>工资项目</b> 中定义工资构成项及计算公式</p>
                    <p>4️⃣ 每月在 <b>工资核算</b> 中一键计算全员工资</p>
                    <p>5️⃣ 在 <b>工资条</b> 中生成并推送工资条给员工</p>
                </div>
            </div>`;
    } catch (e) { el.innerHTML = `<div class="alert alert-danger">加载失败: ${e.message}</div>`; }
}

// ---- 部门管理 ----
async function renderDepartments() {
    const el = document.getElementById('pageContent');
    el.innerHTML = `<div class="loading"><div class="spinner"></div><span class="loading-text">加载中...</span></div>`;
    const res = await API.getDepartments();
    const depts = res.success && res.data ? res.data : [];
    el.innerHTML = `
        <div class="toolbar">
            <div class="toolbar-left">
                <h3 style="font-size:16px;font-weight:600;">共 ${depts.length} 个部门</h3>
            </div>
            <div class="toolbar-right">
                <button class="btn btn-primary" onclick="showDeptModal()">＋ 新增部门</button>
            </div>
        </div>
        <div class="card">
            <div class="table-container">
                <table>
                    <thead><tr><th>部门名称</th><th>编码</th><th>上级部门</th><th>状态</th><th>排序</th><th>操作</th></tr></thead>
                    <tbody>${depts.map(d => `
                        <tr>
                            <td><b>${d.name}</b></td>
                            <td>${d.code || '-'}</td>
                            <td>${d.parent ? d.parent.name : '-'}</td>
                            <td><span class="badge ${d.active ? 'badge-success' : 'badge-danger'}">${d.active ? '启用' : '禁用'}</span></td>
                            <td>${d.sortOrder || '-'}</td>
                            <td>
                                <button class="btn btn-outline btn-sm" onclick="showDeptModal(${d.id})">编辑</button>
                                <button class="btn btn-danger btn-sm" onclick="deleteDept(${d.id})">删除</button>
                            </td>
                        </tr>`).join('') || `<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">🏢</div><h3>暂无部门</h3><p>点击"新增部门"开始创建</p></div></td></tr>`}
                    </tbody>
                </table>
            </div>
        </div>`;
}

function showDeptModal(id) {
    const isEdit = !!id;
    API.getDepartments().then(res => {
        const allDepts = res.success && res.data ? res.data : [];
        let editDept = null;
        if (isEdit) { editDept = allDepts.find(d => d.id === id); }
        const parentOpts = `<option value="">无（顶级部门）</option>${allDepts.filter(d => !isEdit || d.id !== id).map(d => `<option value="${d.id}" ${editDept && editDept.parent && editDept.parent.id === d.id ? 'selected' : ''}>${d.name}</option>`).join('')}`;
        showModal(isEdit ? '编辑部门' : '新增部门', `
            <div class="form-grid">
                <div class="form-group"><label>部门名称 *</label><input id="deptName" value="${editDept ? editDept.name : ''}" placeholder="请输入部门名称"></div>
                <div class="form-group"><label>部门编码</label><input id="deptCode" value="${editDept ? editDept.code || '' : ''}" placeholder="如: TECH"></div>
                <div class="form-group"><label>上级部门</label><select id="deptParent">${parentOpts}</select></div>
                <div class="form-group"><label>排序号</label><input id="deptSort" type="number" value="${editDept ? editDept.sortOrder || '' : ''}" placeholder="数字越小越靠前"></div>
            </div>`,
            `<button class="btn btn-outline" onclick="closeModal()">取消</button>
             <button class="btn btn-primary" onclick="${isEdit ? `saveDept(${id})` : 'saveDept()'}">${isEdit ? '保存修改' : '确认创建'}</button>`
        );
    });
}

async function saveDept(id) {
    const data = {
        name: document.getElementById('deptName').value,
        code: document.getElementById('deptCode').value,
        parent: document.getElementById('deptParent').value ? { id: parseInt(document.getElementById('deptParent').value) } : null,
        sortOrder: document.getElementById('deptSort').value ? parseInt(document.getElementById('deptSort').value) : null
    };
    if (!data.name) { showToast('请输入部门名称', 'error'); return; }
    const res = id ? await API.updateDepartment(id, data) : await API.createDepartment(data);
    if (res.success) { showToast(id ? '部门已更新' : '部门已创建'); closeModal(); renderDepartments(); }
    else showToast(res.message || '操作失败', 'error');
}

async function deleteDept(id) {
    if (!confirm('确定要删除该部门吗？')) return;
    const res = await API.deleteDepartment(id);
    if (res.success) { showToast('部门已删除'); renderDepartments(); }
    else showToast(res.message || '删除失败', 'error');
}

// ---- 员工管理 ----
async function renderEmployees() {
    const el = document.getElementById('pageContent');
    el.innerHTML = `<div class="loading"><div class="spinner"></div><span class="loading-text">加载中...</span></div>`;
    const [empRes, deptRes] = await Promise.all([API.getEmployees({ size: 100 }), API.getActiveDepartments()]);
    const emps = empRes.success && empRes.data ? empRes.data.content || empRes.data : [];
    const depts = deptRes.success && deptRes.data ? deptRes.data : [];
    el.innerHTML = `
        <div class="toolbar">
            <div class="toolbar-left">
                <input class="search-input" placeholder="搜索姓名或工号..." oninput="searchEmployees(this.value)">
                <select class="btn btn-outline" style="padding:9px 12px;" onchange="filterEmpDept(this.value)">
                    <option value="">全部部门</option>${depts.map(d => `<option value="${d.id}">${d.name}</option>`).join('')}
                </select>
            </div>
            <div class="toolbar-right">
                <button class="btn btn-primary" onclick="showEmpModal()">＋ 新增员工</button>
            </div>
        </div>
        <div class="card" id="empTableContainer">
            ${buildEmpTable(emps, depts)}
        </div>`;
}

function buildEmpTable(emps, depts) {
    if (!emps || emps.length === 0) return `<div class="empty-state"><div class="empty-icon">👥</div><h3>暂无员工</h3><p>点击"新增员工"开始添加</p></div>`;
    return `<div class="table-container"><table>
        <thead><tr><th>工号</th><th>姓名</th><th>部门</th><th>岗位</th><th>社保基数</th><th>公积金基数</th><th>社保城市</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>${emps.map(e => `
            <tr>
                <td><b>${e.employeeNo}</b></td><td>${e.name}</td>
                <td>${e.department ? e.department.name : '-'}</td><td>${e.position || '-'}</td>
                <td>${e.socialSecurityBase ? '¥' + e.socialSecurityBase.toLocaleString() : '-'}</td>
                <td>${e.housingFundBase ? '¥' + e.housingFundBase.toLocaleString() : '-'}</td>
                <td>${e.socialSecurityCity || '-'}</td>
                <td><span class="badge ${e.active ? 'badge-success' : 'badge-danger'}">${e.active ? '在职' : '离职'}</span></td>
                <td><button class="btn btn-outline btn-sm" onclick="showEmpModal(${e.id})">编辑</button>
                <button class="btn btn-danger btn-sm" onclick="deleteEmp(${e.id})">删除</button></td>
            </tr>`).join('')}</tbody></table></div>`;
}

function searchEmployees(val) { showToast('搜索功能：' + val); }
function filterEmpDept(val) { showToast('筛选部门ID：' + val); }

function showEmpModal(id) {
    const isEdit = !!id;
    Promise.all([id ? API.getEmployee(id) : Promise.resolve({ data: null }), API.getActiveDepartments()]).then(([empRes, deptRes]) => {
        const emp = isEdit ? (empRes.success && empRes.data ? empRes.data : null) : null;
        const depts = deptRes.success && deptRes.data ? deptRes.data : [];
        const deptOpts = depts.map(d => `<option value="${d.id}" ${emp && emp.department && emp.department.id === d.id ? 'selected' : ''}>${d.name}</option>`).join('');
        showModal(isEdit ? '编辑员工' : '新增员工', `
            <div class="form-grid">
                <div class="form-group"><label>工号 *</label><input id="empNo" value="${emp ? emp.employeeNo : ''}"></div>
                <div class="form-group"><label>姓名 *</label><input id="empName" value="${emp ? emp.name : ''}"></div>
                <div class="form-group"><label>身份证号</label><input id="empIdCard" value="${emp ? emp.idCard || '' : ''}"></div>
                <div class="form-group"><label>手机号</label><input id="empPhone" value="${emp ? emp.phone || '' : ''}"></div>
                <div class="form-group"><label>邮箱</label><input id="empEmail" value="${emp ? emp.email || '' : ''}"></div>
                <div class="form-group"><label>部门</label><select id="empDept"><option value="">请选择</option>${deptOpts}</select></div>
                <div class="form-group"><label>岗位</label><input id="empPos" value="${emp ? emp.position || '' : ''}"></div>
                <div class="form-group"><label>入职日期 *</label><input id="empHire" type="date" value="${emp ? (emp.hireDate || '') : ''}"></div>
                <div class="form-group"><label>社保基数</label><input id="empSsb" type="number" step="0.01" value="${emp && emp.socialSecurityBase ? emp.socialSecurityBase : ''}"></div>
                <div class="form-group"><label>公积金基数</label><input id="empHfb" type="number" step="0.01" value="${emp && emp.housingFundBase ? emp.housingFundBase : ''}"></div>
                <div class="form-group"><label>社保城市</label><input id="empCity" value="${emp ? emp.socialSecurityCity || '' : ''}" placeholder="如: 北京、上海"></div>
            </div>`,
            `<button class="btn btn-outline" onclick="closeModal()">取消</button>
             <button class="btn btn-primary" onclick="${isEdit ? `saveEmp(${id})` : 'saveEmp()'}">${isEdit ? '保存修改' : '确认创建'}</button>`
        );
    });
}

async function saveEmp(id) {
    const deptId = document.getElementById('empDept').value;
    const d = {
        employeeNo: document.getElementById('empNo').value,
        name: document.getElementById('empName').value,
        idCard: document.getElementById('empIdCard').value,
        phone: document.getElementById('empPhone').value,
        email: document.getElementById('empEmail').value,
        department: deptId ? { id: parseInt(deptId) } : null,
        position: document.getElementById('empPos').value,
        hireDate: document.getElementById('empHire').value,
        socialSecurityBase: parseFloat(document.getElementById('empSsb').value) || null,
        housingFundBase: parseFloat(document.getElementById('empHfb').value) || null,
        socialSecurityCity: document.getElementById('empCity').value,
        active: true
    };
    if (!d.employeeNo || !d.name || !d.hireDate) { showToast('请填写工号、姓名和入职日期', 'error'); return; }
    const res = id ? await API.updateEmployee(id, d) : await API.createEmployee(d);
    if (res.success) { showToast(id ? '员工已更新' : '员工已创建'); closeModal(); renderEmployees(); }
    else showToast(res.message || '操作失败', 'error');
}

async function deleteEmp(id) {
    if (!confirm('确定要删除该员工吗？')) return;
    const res = await API.deleteEmployee(id);
    if (res.success) { showToast('员工已删除'); renderEmployees(); }
    else showToast(res.message || '删除失败', 'error');
}

// ---- 工资项目定义 ----
async function renderSalaryItems() {
    const el = document.getElementById('pageContent');
    el.innerHTML = `<div class="loading"><div class="spinner"></div><span class="loading-text">加载中...</span></div>`;
    const res = await API.getSalaryItems();
    const items = res.success && res.data ? res.data : [];
    const typeMap = { EARNING: '应发项', DEDUCTION: '扣款项', TAX: '个税', SOCIAL: '社保', FUND: '公积金', NET: '实发' };
    el.innerHTML = `
        <div class="toolbar">
            <div class="toolbar-left"><h3 style="font-size:16px;font-weight:600;">共 ${items.length} 个工资项目</h3></div>
            <div class="toolbar-right"><button class="btn btn-primary" onclick="showItemModal()">＋ 新增项目</button></div>
        </div>
        <div class="card">
            <div class="table-container"><table>
                <thead><tr><th>项目名称</th><th>编码</th><th>类型</th><th>计算公式</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
                <tbody>${items.map(i => `
                    <tr>
                        <td><b>${i.name}</b></td><td>${i.code || '-'}</td>
                        <td><span class="badge badge-info">${typeMap[i.itemType] || i.itemType}</span></td>
                        <td style="font-family:monospace;font-size:13px;">${i.formula || '-'}</td>
                        <td>${i.sortOrder || '-'}</td>
                        <td><span class="badge ${i.active ? 'badge-success' : 'badge-danger'}">${i.active ? '启用' : '禁用'}</span></td>
                        <td><button class="btn btn-outline btn-sm" onclick="showItemModal(${i.id})">编辑</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteItem(${i.id})">删除</button></td>
                    </tr>`).join('') || '<tr><td colspan="7"><div class="empty-state"><div class="empty-icon">📋</div><h3>暂无工资项目</h3></div></td></tr>'}
                </tbody>
            </table></div>
        </div>`;
}

function showItemModal(id) {
    const isEdit = !!id;
    const promise = isEdit ? API.getSalaryItem(id) : Promise.resolve({ data: null });
    promise.then(res => {
        const item = isEdit && res.success ? res.data : null;
        showModal(isEdit ? '编辑工资项目' : '新增工资项目', `
            <div class="form-grid">
                <div class="form-group"><label>项目名称 *</label><input id="itemName" value="${item ? item.name : ''}" placeholder="如: 高温补贴"></div>
                <div class="form-group"><label>项目编码</label><input id="itemCode" value="${item ? item.code || '' : ''}" placeholder="如: heatSubsidy"></div>
                <div class="form-group"><label>项目类型 *</label><select id="itemType">
                    ${['EARNING','DEDUCTION','TAX','SOCIAL','FUND','NET'].map(t => `<option value="${t}" ${item && item.itemType === t ? 'selected' : ''}>${t}</option>`).join('')}
                </select></div>
                <div class="form-group"><label>排序号</label><input id="itemSort" type="number" value="${item ? item.sortOrder || '' : ''}"></div>
                <div class="form-group full-width"><label>计算公式</label>
                    <textarea id="itemFormula" rows="3" style="font-family:monospace;" placeholder="如: basePay+seniorityPay+meritPay">${item ? item.formula || '' : ''}</textarea>
                    <span style="font-size:12px;color:var(--gray-400);margin-top:4px;">使用编码引用其他工资项，支持 + - 运算</span>
                </div>
            </div>`,
            `<button class="btn btn-outline" onclick="closeModal()">取消</button>
             <button class="btn btn-primary" onclick="${isEdit ? `saveItem(${id})` : 'saveItem()'}">${isEdit ? '保存修改' : '确认创建'}</button>`
        );
    });
}

async function saveItem(id) {
    const d = {
        name: document.getElementById('itemName').value,
        code: document.getElementById('itemCode').value,
        itemType: document.getElementById('itemType').value,
        formula: document.getElementById('itemFormula').value,
        sortOrder: parseInt(document.getElementById('itemSort').value) || null
    };
    if (!d.name) { showToast('请输入项目名称', 'error'); return; }
    const res = id ? await API.updateSalaryItem(id, d) : await API.createSalaryItem(d);
    if (res.success) { showToast(id ? '项目已更新' : '项目已创建'); closeModal(); renderSalaryItems(); }
    else showToast(res.message || '操作失败', 'error');
}

async function deleteItem(id) {
    if (!confirm('确定要删除该工资项目吗？')) return;
    const res = await API.deleteSalaryItem(id);
    if (res.success) { showToast('项目已删除'); renderSalaryItems(); }
    else showToast(res.message || '删除失败', 'error');
}

// ---- 工资核算 ----
async function renderPayroll() {
    const el = document.getElementById('pageContent');
    const now = new Date();
    const currentYm = now.getFullYear() + String(now.getMonth() + 1).padStart(2, '0');
    el.innerHTML = `
        <div class="tabs">
            <button class="tab active" onclick="switchPayrollTab('calc',this)">🧮 工资计算</button>
            <button class="tab" onclick="switchPayrollTab('query',this)">📋 记录查询</button>
            <button class="tab" onclick="switchPayrollTab('import',this)">📥 数据导入</button>
        </div>
        <div id="payrollContent">
            ${renderPayrollCalc(currentYm)}
        </div>`;
}

function switchPayrollTab(tab, btn) {
    document.querySelectorAll('.tabs .tab').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
    const now = new Date();
    const ym = now.getFullYear() + String(now.getMonth() + 1).padStart(2, '0');
    const html = tab === 'calc' ? renderPayrollCalc(ym) : tab === 'query' ? renderPayrollQuery(ym) : renderPayrollImport();
    document.getElementById('payrollContent').innerHTML = html;
}

function renderPayrollCalc(ym) {
    return `
        <div class="card">
            <div class="card-header"><h3>工资计算</h3></div>
            <div class="card-body">
                <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                    <div class="form-group"><label>计算年月</label><input id="calcYm" type="month" value="${ym.substring(0,4)}-${ym.substring(4)}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
                </div>
                <div style="display:flex;gap:12px;">
                    <button class="btn btn-primary" onclick="doCalculate(true)">🧪 试算</button>
                    <button class="btn btn-success" onclick="doCalculate(false)">✅ 正式计算</button>
                </div>
                <div id="calcResult" style="margin-top:16px;"></div>
            </div>
        </div>`;
}

function renderPayrollQuery(ym) {
    return `
        <div class="card">
            <div class="card-header"><h3>工资记录查询</h3></div>
            <div class="card-body">
                <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                    <div class="form-group"><label>年月</label><input id="queryYm" type="month" value="${ym.substring(0,4)}-${ym.substring(4)}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
                </div>
                <button class="btn btn-primary" onclick="doQuery()">🔍 查询</button>
                <div id="queryResult" style="margin-top:16px;"></div>
            </div>
        </div>`;
}

function renderPayrollImport() {
    return `
        <div class="card">
            <div class="card-header"><h3>Excel 数据导入</h3></div>
            <div class="card-body">
                <div class="alert alert-info">📌 支持导入考勤扣款、绩效积分等可变数据。Excel格式：第一列为员工工号，后续列为可变工资项值。</div>
                <div class="form-group" style="margin-bottom:16px;">
                    <label>选择Excel文件</label>
                    <input id="importFile" type="file" accept=".xlsx,.xls" style="padding:9px 0;">
                </div>
                <button class="btn btn-primary" onclick="doImport()">📥 开始导入</button>
                <div id="importResult" style="margin-top:16px;"></div>
            </div>
        </div>`;
}

async function doCalculate(trial) {
    const ym = document.getElementById('calcYm').value.replace('-', '');
    const btn = event.target;
    btn.disabled = true; btn.textContent = '计算中...';
    const res = await API.calculateSalary({ yearMonth: ym, trialRun: trial, variableValues: {} });
    btn.disabled = false; btn.textContent = trial ? '🧪 试算' : '✅ 正式计算';
    const el = document.getElementById('calcResult');
    if (res.success && res.data) {
        const totalPay = res.data.reduce((s, r) => s + (r.netPay || 0), 0);
        const totalGross = res.data.reduce((s, r) => s + (r.grossPay || 0), 0);
        el.innerHTML = `
            <div class="alert alert-success">✅ ${trial ? '试算' : '计算'}完成！共处理 ${res.data.length} 名员工，应发合计 ¥${totalGross.toFixed(2)}，实发合计 ¥${totalPay.toFixed(2)}</div>
            <div class="payroll-result">
                ${res.data.map(r => `
                    <div class="payroll-item">
                        <span><b>${r.employee.name}</b> (${r.employee.employeeNo}) - ${r.employee.department.name}</span>
                        <span>应发: ¥${(r.grossPay||0).toFixed(2)} | 个税: ¥${(r.incomeTax||0).toFixed(2)} | 实发: <b style="color:var(--danger)">¥${(r.netPay||0).toFixed(2)}</b></span>
                    </div>`).join('')}
            </div>`;
    } else {
        el.innerHTML = `<div class="alert alert-danger">${res.message || '计算失败'}</div>`;
    }
}

async function doQuery() {
    const ym = document.getElementById('queryYm').value.replace('-', '');
    const el = document.getElementById('queryResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    const res = await API.getPayrollRecords(ym);
    if (res.success && res.data) {
        el.innerHTML = `<div class="alert alert-info">共 ${res.data.length} 条记录</div>
            <div class="table-container"><table>
                <thead><tr><th>姓名</th><th>工号</th><th>部门</th><th>应发</th><th>个税</th><th>社保</th><th>公积金</th><th>实发</th><th>状态</th></tr></thead>
                <tbody>${res.data.map(r => `
                    <tr>
                        <td><b>${r.employee.name}</b></td><td>${r.employee.employeeNo}</td>
                        <td>${r.employee.department.name}</td>
                        <td>¥${(r.grossPay||0).toFixed(2)}</td><td>¥${(r.incomeTax||0).toFixed(2)}</td>
                        <td>¥${(r.socialInsurance||0).toFixed(2)}</td><td>¥${(r.housingFund||0).toFixed(2)}</td>
                        <td><b>¥${(r.netPay||0).toFixed(2)}</b></td>
                        <td><span class="badge ${r.status === 'CONFIRMED' ? 'badge-success' : r.status === 'PAID' ? 'badge-info' : 'badge-warning'}">${r.status === 'DRAFT' ? '试算' : r.status === 'CONFIRMED' ? '已确认' : '已发放'}</span></td>
                    </tr>`).join('')}</tbody></table></div>`;
    } else {
        el.innerHTML = `<div class="alert alert-danger">${res.message || '暂无数据'}</div>`;
    }
}

async function doImport() {
    const file = document.getElementById('importFile').files[0];
    if (!file) { showToast('请选择Excel文件', 'error'); return; }
    const el = document.getElementById('importResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div><span class="loading-text">导入中...</span></div>`;
    const res = await API.importPayroll(file);
    if (res.success) {
        el.innerHTML = `<div class="alert alert-success">✅ 导入成功！</div>`;
        showToast('数据导入成功');
    } else {
        el.innerHTML = `<div class="alert alert-danger">${res.message || '导入失败'}</div>`;
    }
}

// ---- 工资条 ----
async function renderPayslips() {
    const el = document.getElementById('pageContent');
    const now = new Date();
    const ym = now.getFullYear() + String(now.getMonth() + 1).padStart(2, '0');
    el.innerHTML = `
        <div class="toolbar"><div class="toolbar-left"><h3 style="font-size:16px;font-weight:600;">工资条管理</h3></div></div>
        <div class="card">
            <div class="card-body">
                <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                    <div class="form-group"><label>查询年月</label><input id="slipYm" type="month" value="${ym.substring(0,4)}-${ym.substring(4)}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
                    <div class="form-group"><label>员工工号</label><input id="slipEmpNo" placeholder="留空查询全部" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
                </div>
                <button class="btn btn-primary" onclick="queryPayslips()">🔍 查询</button>
                <div id="slipResult" style="margin-top:16px;"></div>
            </div>
        </div>`;
}

async function queryPayslips() {
    const ym = document.getElementById('slipYm').value.replace('-', '');
    const el = document.getElementById('slipResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    const res = await API.getPayrollRecords(ym);
    if (res.success && res.data && res.data.length > 0) {
        el.innerHTML = `<div class="table-container"><table>
            <thead><tr><th>姓名</th><th>工号</th><th>部门</th><th>应发</th><th>实发</th><th>操作</th></tr></thead>
            <tbody>${res.data.map(r => `
                <tr>
                    <td><b>${r.employee.name}</b></td><td>${r.employee.employeeNo}</td>
                    <td>${r.employee.department.name}</td>
                    <td>¥${(r.grossPay||0).toFixed(2)}</td><td><b>¥${(r.netPay||0).toFixed(2)}</b></td>
                    <td>
                        <button class="btn btn-outline btn-sm" onclick="viewSlipHtml(${r.id})">👁️ 预览</button>
                        <button class="btn btn-outline btn-sm" onclick="API.getPayslipPdf(${r.id})">📄 PDF</button>
                        <button class="btn btn-success btn-sm" onclick="pushSlip(${r.id})">📤 推送</button>
                    </td>
                </tr>`).join('')}</tbody></table></div>`;
    } else {
        el.innerHTML = `<div class="empty-state"><div class="empty-icon">📄</div><h3>暂无工资条数据</h3><p>请先在工资核算中计算工资</p></div>`;
    }
}

async function viewSlipHtml(recordId) {
    const res = await API.getPayslipHtml(recordId);
    if (res.success && res.data) {
        showModal('工资条预览', `<div class="slip-preview">${res.data}</div>`);
    } else {
        showToast('获取工资条失败', 'error');
    }
}

async function pushSlip(recordId) {
    const res = await API.pushPayslip(recordId);
    if (res.success) { showToast('工资条已推送'); }
    else showToast(res.message || '推送失败', 'error');
}

// ---- 统计报表 ----
async function renderReports() {
    const el = document.getElementById('pageContent');
    const now = new Date(), year = now.getFullYear();
    const ym = year + String(now.getMonth() + 1).padStart(2, '0');
    el.innerHTML = `
        <div class="tabs">
            <button class="tab active" onclick="switchReportTab('dept',this)">🏢 部门汇总</button>
            <button class="tab" onclick="switchReportTab('tax',this)">📋 个税申报</button>
            <button class="tab" onclick="switchReportTab('yearly',this)">📈 年度对比</button>
        </div>
        <div id="reportContent">${renderDeptReport(ym)}</div>`;
}

function switchReportTab(tab, btn) {
    document.querySelectorAll('.tabs .tab').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
    const now = new Date(), year = now.getFullYear();
    const ym = year + String(now.getMonth() + 1).padStart(2, '0');
    const html = tab === 'dept' ? renderDeptReport(ym) : tab === 'tax' ? renderTaxReport(ym) : renderYearlyReport(year);
    document.getElementById('reportContent').innerHTML = html;
}

function renderDeptReport(ym) {
    return `<div class="card">
        <div class="card-header"><h3>部门工资汇总</h3></div>
        <div class="card-body">
            <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                <div class="form-group"><label>年月</label><input id="rptYm" type="month" value="${ym.substring(0,4)}-${ym.substring(4)}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
            </div>
            <button class="btn btn-primary" onclick="loadDeptReport()">📊 生成报表</button>
            <div id="deptRptResult" style="margin-top:16px;"></div>
        </div>
    </div>`;
}

function renderTaxReport(ym) {
    return `<div class="card">
        <div class="card-header"><h3>个税申报表 <button class="btn btn-outline btn-sm" onclick="API.exportTaxReport(document.getElementById('taxYm').value.replace('-',''))">📥 导出Excel</button></h3></div>
        <div class="card-body">
            <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                <div class="form-group"><label>年月</label><input id="taxYm" type="month" value="${ym.substring(0,4)}-${ym.substring(4)}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
            </div>
            <button class="btn btn-primary" onclick="loadTaxReport()">📋 生成报表</button>
            <div id="taxRptResult" style="margin-top:16px;"></div>
        </div>
    </div>`;
}

function renderYearlyReport(year) {
    return `<div class="card">
        <div class="card-header"><h3>年度对比分析</h3></div>
        <div class="card-body">
            <div class="form-grid form-grid-3" style="margin-bottom:16px;">
                <div class="form-group"><label>年份</label><input id="yearlyYear" type="number" value="${year}" style="padding:9px 12px;border:1px solid var(--gray-300);border-radius:8px;font-size:14px;"></div>
            </div>
            <button class="btn btn-primary" onclick="loadYearlyReport()">📈 生成分析</button>
            <div id="yearlyRptResult" style="margin-top:16px;"></div>
        </div>
    </div>`;
}

async function loadDeptReport() {
    const ym = document.getElementById('rptYm').value.replace('-', '');
    const el = document.getElementById('deptRptResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    const res = await API.getDeptSummary(ym);
    if (res.success && res.data) {
        el.innerHTML = `<div class="table-container"><table>
            <thead><tr><th>部门</th><th>人数</th><th>应发合计</th><th>个税</th><th>社保</th><th>公积金</th><th>实发合计</th></tr></thead>
            <tbody>${res.data.filter(d => d.employeeCount > 0).map(d => `
                <tr>
                    <td><b>${d.departmentName}</b></td><td>${d.employeeCount}</td>
                    <td>¥${(d.grossPay||0).toFixed(2)}</td><td>¥${(d.totalTax||0).toFixed(2)}</td>
                    <td>¥${(d.totalSocial||0).toFixed(2)}</td><td>¥${(d.totalFund||0).toFixed(2)}</td>
                    <td><b>¥${(d.netPay||0).toFixed(2)}</b></td>
                </tr>`).join('')}</tbody></table></div>`;
    } else {
        el.innerHTML = `<div class="empty-state"><div class="empty-icon">📊</div><h3>暂无数据</h3><p>请先进行工资核算</p></div>`;
    }
}

async function loadTaxReport() {
    const ym = document.getElementById('taxYm').value.replace('-', '');
    const el = document.getElementById('taxRptResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    const res = await API.getTaxReport(ym);
    if (res.success && res.data) {
        el.innerHTML = `<div class="table-container"><table>
            <thead><tr><th>姓名</th><th>身份证号</th><th>工资号</th><th>累计收入</th><th>累计扣除</th><th>累计已缴</th><th>本月应缴</th><th>实发</th></tr></thead>
            <tbody>${res.data.map(r => `<tr>${Object.values(r).map(v => `<td>${v ?? '-'}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`;
    } else {
        el.innerHTML = `<div class="empty-state"><div class="empty-icon">📋</div><h3>暂无数据</h3></div>`;
    }
}

async function loadYearlyReport() {
    const year = document.getElementById('yearlyYear').value;
    const el = document.getElementById('yearlyRptResult');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    const res = await API.getYearlyComparison(year);
    if (res.success && res.data) {
        const grouped = {};
        res.data.forEach(d => { if (!grouped[d.departmentName]) grouped[d.departmentName] = []; grouped[d.departmentName].push(d); });
        el.innerHTML = Object.entries(grouped).map(([dept, rows]) => `
            <div class="card" style="margin-bottom:12px;">
                <div class="card-header"><h3>${dept}</h3></div>
                <div class="table-container"><table>
                    <thead><tr><th>月份</th><th>应发合计</th><th>实发合计</th></tr></thead>
                    <tbody>${rows.map(r => `
                        <tr><td>${r.month}</td><td>¥${(r.grossPay||0).toFixed(2)}</td><td><b>¥${(r.netPay||0).toFixed(2)}</b></td></tr>
                    `).join('')}</tbody></table></div>
            </div>`).join('');
    } else {
        el.innerHTML = `<div class="empty-state"><div class="empty-icon">📈</div><h3>暂无年度数据</h3></div>`;
    }
}

// ---- 系统设置 ----
async function renderSystem() {
    const el = document.getElementById('pageContent');
    el.innerHTML = `
        <div class="tabs">
            <button class="tab active" onclick="switchSysTab('users',this)">👤 用户管理</button>
            <button class="tab" onclick="switchSysTab('social',this)">🏥 社保配置</button>
            <button class="tab" onclick="switchSysTab('logs',this)">📝 操作日志</button>
        </div>
        <div id="sysContent"><div class="loading"><div class="spinner"></div></div></div>`;
    switchSysTab('users', document.querySelector('.tabs .tab'));
}

function switchSysTab(tab, btn) {
    document.querySelectorAll('#sysContent ~ .tabs .tab, .tabs .tab').forEach(t => t.classList.remove('active'));
    if (btn) btn.classList.add('active');
    const el = document.getElementById('sysContent');
    el.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    if (tab === 'users') loadUsers();
    else if (tab === 'social') loadSocialConfig();
    else loadLogs();
}

async function loadUsers() {
    const el = document.getElementById('sysContent');
    const res = await API.getUsers();
    const users = res.success && res.data ? res.data : [];
    const roleMap = { ADMIN: '管理员', FINANCE: '财务', HR: '人事', EMPLOYEE: '员工' };
    el.innerHTML = `
        <div class="toolbar"><div class="toolbar-left"><h3 style="font-size:16px;font-weight:600;">共 ${users.length} 个用户</h3></div>
        <div class="toolbar-right"><button class="btn btn-primary" onclick="showUserModal()">＋ 新增用户</button></div></div>
        <div class="card"><div class="table-container"><table>
            <thead><tr><th>用户名</th><th>姓名</th><th>邮箱</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>${users.map(u => `
                <tr>
                    <td><b>${u.username}</b></td><td>${u.realName || '-'}</td><td>${u.email || '-'}</td>
                    <td><span class="badge ${u.role === 'ADMIN' ? 'badge-danger' : u.role === 'FINANCE' ? 'badge-warning' : 'badge-info'}">${roleMap[u.role] || u.role}</span></td>
                    <td><span class="badge ${u.active ? 'badge-success' : 'badge-danger'}">${u.active ? '启用' : '禁用'}</span></td>
                    <td><button class="btn btn-outline btn-sm" onclick="showUserModal(${u.id})">编辑</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id})">删除</button></td>
                </tr>`).join('')}</tbody></table></div></div>`;
}

function showUserModal(id) {
    const isEdit = !!id;
    const promise = isEdit ? API.getUsers().then(r => ({ data: r.data?.find(u => u.id === id) })) : Promise.resolve({ data: null });
    promise.then(res => {
        const u = res.data;
        showModal(isEdit ? '编辑用户' : '新增用户', `
            <div class="form-grid">
                <div class="form-group"><label>用户名 *</label><input id="uName" value="${u ? u.username : ''}"></div>
                <div class="form-group"><label>密码 ${isEdit ? '(留空不修改)' : '*'}</label><input id="uPass" type="password"></div>
                <div class="form-group"><label>真实姓名</label><input id="uReal" value="${u ? u.realName || '' : ''}"></div>
                <div class="form-group"><label>邮箱</label><input id="uEmail" value="${u ? u.email || '' : ''}"></div>
                <div class="form-group"><label>角色 *</label><select id="uRole">
                    ${['ADMIN','FINANCE','HR','EMPLOYEE'].map(r => `<option value="${r}" ${u && u.role === r ? 'selected' : ''}>${r}</option>`).join('')}
                </select></div>
                <div class="form-group"><label>状态</label><select id="uActive"><option value="true" ${u && u.active ? 'selected' : ''}>启用</option><option value="false" ${u && !u.active ? 'selected' : ''}>禁用</option></select></div>
            </div>`,
            `<button class="btn btn-outline" onclick="closeModal()">取消</button>
             <button class="btn btn-primary" onclick="${isEdit ? `saveUser(${id})` : 'saveUser()'}">${isEdit ? '保存' : '创建'}</button>`
        );
    });
}

async function saveUser(id) {
    const d = {
        username: document.getElementById('uName').value,
        password: document.getElementById('uPass').value,
        realName: document.getElementById('uReal').value,
        email: document.getElementById('uEmail').value,
        role: document.getElementById('uRole').value,
        active: document.getElementById('uActive').value === 'true'
    };
    if (!d.username) { showToast('请输入用户名', 'error'); return; }
    if (!id && !d.password) { showToast('请设置密码', 'error'); return; }
    if (!id && !d.password) d.password = '123456';
    const res = id ? await API.updateUser(id, d) : await API.createUser(d);
    if (res.success) { showToast(id ? '用户已更新' : '用户已创建'); closeModal(); loadUsers(); }
    else showToast(res.message || '操作失败', 'error');
}

async function deleteUser(id) {
    if (!confirm('确定删除该用户吗？')) return;
    const res = await API.deleteUser(id);
    if (res.success) { showToast('用户已删除'); loadUsers(); }
    else showToast(res.message || '删除失败', 'error');
}

async function loadSocialConfig() {
    const el = document.getElementById('sysContent');
    const res = await API.getSocialSecurity();
    const cfgs = res.success && res.data ? res.data : [];
    const typeMap = { ENDOWMENT: '养老', MEDICAL: '医疗', UNEMPLOYMENT: '失业', INJURY: '工伤', MATERNITY: '生育', HOUSING_FUND: '公积金' };
    el.innerHTML = `
        <div class="toolbar"><div class="toolbar-left"><h3 style="font-size:16px;font-weight:600;">社保/公积金配置</h3></div>
        <div class="toolbar-right"><button class="btn btn-primary" onclick="showSocialModal()">＋ 新增配置</button></div></div>
        <div class="card"><div class="table-container"><table>
            <thead><tr><th>城市</th><th>险种</th><th>个人比例</th><th>单位比例</th><th>基数下限</th><th>基数上限</th><th>年份</th><th>操作</th></tr></thead>
            <tbody>${cfgs.filter(c => c.active).map(c => `
                <tr>
                    <td><b>${c.city || '默认'}</b></td><td>${typeMap[c.insuranceType] || c.insuranceType}</td>
                    <td>${c.personalRate}%</td><td>${c.companyRate}%</td>
                    <td>${c.baseLower ? '¥' + c.baseLower.toLocaleString() : '-'}</td>
                    <td>${c.baseUpper ? '¥' + c.baseUpper.toLocaleString() : '-'}</td>
                    <td>${c.year}</td>
                    <td><button class="btn btn-outline btn-sm" onclick="showSocialModal(${c.id})">编辑</button></td>
                </tr>`).join('') || '<tr><td colspan="8"><div class="empty-state"><div class="empty-icon">🏥</div><h3>暂无配置</h3></div></td></tr>'}
            </tbody></table></div></div>`;
}

function showSocialModal(id) {
    const isEdit = !!id;
    const promise = isEdit ? API.getSocialSecurity().then(r => ({ data: r.data?.find(c => c.id === id) })) : Promise.resolve({ data: null });
    promise.then(res => {
        const c = res.data;
        showModal(isEdit ? '编辑社保配置' : '新增社保配置', `
            <div class="form-grid">
                <div class="form-group"><label>城市</label><input id="ssCity" value="${c ? c.city || '' : ''}" placeholder="如: 北京"></div>
                <div class="form-group"><label>险种 *</label><select id="ssType">
                    ${['ENDOWMENT','MEDICAL','UNEMPLOYMENT','INJURY','MATERNITY','HOUSING_FUND'].map(t => `<option value="${t}" ${c && c.insuranceType === t ? 'selected' : ''}>${t}</option>`).join('')}
                </select></div>
                <div class="form-group"><label>个人比例(%) *</label><input id="ssPer" type="number" step="0.1" value="${c ? c.personalRate : ''}"></div>
                <div class="form-group"><label>单位比例(%) *</label><input id="ssCom" type="number" step="0.1" value="${c ? c.companyRate : ''}"></div>
                <div class="form-group"><label>基数下限</label><input id="ssLow" type="number" step="0.01" value="${c && c.baseLower ? c.baseLower : ''}"></div>
                <div class="form-group"><label>基数上限</label><input id="ssUp" type="number" step="0.01" value="${c && c.baseUpper ? c.baseUpper : ''}"></div>
                <div class="form-group"><label>年份 *</label><input id="ssYear" type="number" value="${c ? c.year : new Date().getFullYear()}"></div>
            </div>`,
            `<button class="btn btn-outline" onclick="closeModal()">取消</button>
             <button class="btn btn-primary" onclick="${isEdit ? `saveSocial(${id})` : 'saveSocial()'}">${isEdit ? '保存' : '创建'}</button>`
        );
    });
}

async function saveSocial(id) {
    const d = {
        city: document.getElementById('ssCity').value,
        insuranceType: document.getElementById('ssType').value,
        personalRate: parseFloat(document.getElementById('ssPer').value),
        companyRate: parseFloat(document.getElementById('ssCom').value),
        baseLower: parseFloat(document.getElementById('ssLow').value) || null,
        baseUpper: parseFloat(document.getElementById('ssUp').value) || null,
        year: parseInt(document.getElementById('ssYear').value),
        active: true
    };
    if (!d.personalRate || !d.companyRate || !d.year) { showToast('请填写必要字段', 'error'); return; }
    const res = id ? await API.updateSocialSecurity(id, d) : await API.createSocialSecurity(d);
    if (res.success) { showToast(id ? '配置已更新' : '配置已创建'); closeModal(); loadSocialConfig(); }
    else showToast(res.message || '操作失败', 'error');
}

async function loadLogs(page) {
    const el = document.getElementById('sysContent');
    const res = await API.getLogs(page || 0);
    const logs = res.success && res.data ? res.data.content || res.data : [];
    el.innerHTML = `
        <div class="card">
            <div class="card-header"><h3>操作日志</h3></div>
            <div class="card-body">
                <div class="log-viewer">
                    ${(Array.isArray(logs) ? logs : []).map(l => `
                        <div class="log-entry">
                            <span class="log-time">${l.createTime ? new Date(l.createTime).toLocaleString() : '-'}</span>
                            <span class="log-level ${l.result}">[${l.result}]</span>
                            <span><b>${l.username}</b> ${l.operation}</span>
                            <span style="color:var(--gray-400)">${l.detail || ''}</span>
                        </div>`).join('') || '<div style="text-align:center;padding:20px;color:var(--gray-400);">暂无日志记录</div>'}
                </div>
                ${res.data && res.data.totalPages > 1 ? `
                <div style="margin-top:12px;display:flex;gap:8px;justify-content:center;">
                    ${Array.from({length: res.data.totalPages}, (_, i) => `
                        <button class="btn btn-outline btn-sm ${i === (page || 0) ? 'btn-primary' : ''}" onclick="loadLogs(${i})">${i+1}</button>
                    `).join('')}
                </div>` : ''}
            </div>
        </div>`;
}
