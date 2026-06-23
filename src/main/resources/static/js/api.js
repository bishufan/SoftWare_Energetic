// ============================================
// API Layer - 所有后端接口调用
// ============================================
const API = {
    _token: null,
    _base: 'http://localhost:8080/api',

    setToken(token) { this._token = token; localStorage.setItem('auth_token', token); },
    getToken() { return this._token || localStorage.getItem('auth_token'); },
    clearToken() { this._token = null; localStorage.removeItem('auth_token'); },

    async _fetch(method, path, body) {
        const opts = { method, headers: { 'Content-Type': 'application/json' } };
        const token = this.getToken();
        if (token) opts.headers['Authorization'] = 'Bearer ' + token;
        if (body) opts.body = JSON.stringify(body);
        try {
            const res = await fetch(this._base + path, opts);
            const data = await res.json();
            if (!data.success && data.message) {
                if (data.message.includes('未授权') || data.message.includes('无效')) {
                    handleLogout();
                }
            }
            return data;
        } catch (e) {
            return { success: false, message: '网络错误: ' + e.message, data: null };
        }
    },

    get(path) { return this._fetch('GET', path); },
    post(path, body) { return this._fetch('POST', path, body); },
    put(path, body) { return this._fetch('PUT', path, body); },
    del(path) { return this._fetch('DELETE', path); },

    // ---- Auth ----
    async login(username, password) {
        const res = await this.post('/auth/login', { username, password });
        if (res.success && res.data && res.data.token) this.setToken(res.data.token);
        return res;
    },

    // ---- Departments ----
    getDepartments() { return this.get('/departments'); },
    getActiveDepartments() { return this.get('/departments/active'); },
    getDepartment(id) { return this.get('/departments/' + id); },
    createDepartment(data) { return this.post('/departments', data); },
    updateDepartment(id, data) { return this.put('/departments/' + id, data); },
    deleteDepartment(id) { return this.del('/departments/' + id); },

    // ---- Employees ----
    getEmployees(params) {
        const q = new URLSearchParams(params).toString();
        return this.get('/employees?' + q);
    },
    getAllEmployees() { return this.get('/employees/all'); },
    getEmployee(id) { return this.get('/employees/' + id); },
    createEmployee(data) { return this.post('/employees', data); },
    updateEmployee(id, data) { return this.put('/employees/' + id, data); },
    deleteEmployee(id) { return this.del('/employees/' + id); },

    // ---- Salary Items ----
    getSalaryItems() { return this.get('/salary-items'); },
    getSalaryItemsByType(type) { return this.get('/salary-items/type/' + type); },
    getSalaryItem(id) { return this.get('/salary-items/' + id); },
    createSalaryItem(data) { return this.post('/salary-items', data); },
    updateSalaryItem(id, data) { return this.put('/salary-items/' + id, data); },
    deleteSalaryItem(id) { return this.del('/salary-items/' + id); },

    // ---- Payroll ----
    calculateSalary(data) { return this.post('/payroll/calculate', data); },
    getPayrollRecords(yearMonth, departmentId) {
        let path = '/payroll/records?yearMonth=' + yearMonth;
        if (departmentId) path += '&departmentId=' + departmentId;
        return this.get(path);
    },
    getEmployeeRecords(empId) { return this.get('/payroll/records/employee/' + empId); },
    getEmployeeMonthRecord(empId, ym) { return this.get('/payroll/records/employee/' + empId + '/' + ym); },
    confirmPayroll(id) { return this.post('/payroll/records/' + id + '/confirm', {}); },
    async importPayroll(file) {
        const token = this.getToken();
        const formData = new FormData();
        formData.append('file', file);
        const res = await fetch(this._base + '/payroll/import', { method: 'POST', headers: { 'Authorization': 'Bearer ' + token }, body: formData });
        return await res.json();
    },
    async exportPayroll(yearMonth, departmentId) {
        const token = this.getToken();
        let path = this._base + '/payroll/export?yearMonth=' + yearMonth;
        if (departmentId) path += '&departmentId=' + departmentId;
        const res = await fetch(path, { headers: { 'Authorization': 'Bearer ' + token } });
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a'); a.href = url; a.download = 'salary_' + yearMonth + '.xlsx'; a.click();
        URL.revokeObjectURL(url);
    },

    // ---- Payslips ----
    getPayslipPdf(recordId) {
        const token = this.getToken();
        const path = this._base + '/payslips/' + recordId + '/pdf';
        fetch(path, { headers: { 'Authorization': 'Bearer ' + token } })
            .then(res => res.blob())
            .then(blob => {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a'); a.href = url; a.download = 'payslip_' + recordId + '.pdf'; a.click();
                URL.revokeObjectURL(url);
            });
    },
    getPayslipHtml(recordId) { return this.get('/payslips/' + recordId + '/html'); },
    pushPayslip(recordId) { return this.post('/payslips/' + recordId + '/push', {}); },

    // ---- Reports ----
    getDeptSummary(yearMonth) { return this.get('/reports/department-summary?yearMonth=' + yearMonth); },
    getTaxReport(yearMonth) { return this.get('/reports/tax-report?yearMonth=' + yearMonth); },
    async exportTaxReport(yearMonth) {
        const token = this.getToken();
        const path = this._base + '/reports/tax-report/export?yearMonth=' + yearMonth;
        const res = await fetch(path, { headers: { 'Authorization': 'Bearer ' + token } });
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'tax_report_' + yearMonth + '.xlsx';
        a.click();
        URL.revokeObjectURL(url);
    },
    getYearlyComparison(year) { return this.get('/reports/yearly-comparison?year=' + year); },

    // ---- System ----
    getUsers() { return this.get('/system/users'); },
    createUser(data) { return this.post('/system/users', data); },
    updateUser(id, data) { return this.put('/system/users/' + id, data); },
    deleteUser(id) { return this.del('/system/users/' + id); },
    getSocialSecurity(city, year) {
        let path = '/system/social-security';
        const p = [];
        if (city) p.push('city=' + city);
        if (year) p.push('year=' + year);
        if (p.length) path += '?' + p.join('&');
        return this.get(path);
    },
    createSocialSecurity(data) { return this.post('/system/social-security', data); },
    updateSocialSecurity(id, data) { return this.put('/system/social-security/' + id, data); },
    getLogs(page) { return this.get('/system/logs?page=' + (page || 0)); },
};
