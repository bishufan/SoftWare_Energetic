package com.salary.common.config;

import com.salary.module.org.domain.Department;
import com.salary.module.org.domain.Employee;
import com.salary.module.org.repository.DepartmentRepository;
import com.salary.module.org.repository.EmployeeRepository;
import com.salary.module.salary.item.domain.SalaryItem;
import com.salary.module.salary.item.SalaryItemRepository;
import com.salary.module.system.domain.SocialSecurityConfig;
import com.salary.module.system.domain.TaxConfig;
import com.salary.module.system.domain.User;
import com.salary.module.system.repository.SocialSecurityConfigRepository;
import com.salary.module.system.repository.TaxConfigRepository;
import com.salary.module.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SocialSecurityConfigRepository socialSecurityRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final SalaryItemRepository salaryItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据库已初始化，跳过初始化数据");
            return;
        }
        log.info("开始初始化系统数据...");
        initUsers();
        List<Department> departments = initDepartments();
        initEmployees(departments);
        initSalaryItems();
        initSocialSecurity();
        initTaxConfig();
        log.info("系统数据初始化完成！");
    }

    private void initUsers() {
        userRepository.save(User.builder().username("admin").password(passwordEncoder.encode("admin123"))
                .realName("系统管理员").role("ADMIN").active(true).build());
        userRepository.save(User.builder().username("finance").password(passwordEncoder.encode("finance123"))
                .realName("财务主管").role("FINANCE").active(true).build());
        userRepository.save(User.builder().username("hr").password(passwordEncoder.encode("hr123"))
                .realName("人事经理").role("HR").active(true).build());
        userRepository.save(User.builder().username("employee1").password(passwordEncoder.encode("emp123"))
                .realName("张三").role("EMPLOYEE").active(true).build());
        userRepository.save(User.builder().username("employee2").password(passwordEncoder.encode("emp123"))
                .realName("李四").role("EMPLOYEE").active(true).build());
    }

    private List<Department> initDepartments() {
        Department tech = departmentRepository.save(
                Department.builder().name("技术部").code("TECH").sortOrder(1).active(true).build());
        Department sales = departmentRepository.save(
                Department.builder().name("销售部").code("SALES").sortOrder(2).active(true).build());
        Department finance = departmentRepository.save(
                Department.builder().name("财务部").code("FIN").sortOrder(3).active(true).build());
        Department hr = departmentRepository.save(
                Department.builder().name("人事部").code("HR").sortOrder(4).active(true).build());
        departmentRepository.save(Department.builder().name("前端组").code("FE").parent(tech).sortOrder(1).active(true).build());
        departmentRepository.save(Department.builder().name("后端组").code("BE").parent(tech).sortOrder(2).active(true).build());
        return List.of(tech, sales, finance, hr);
    }

    private void initEmployees(List<Department> departments) {
        employeeRepository.save(Employee.builder().employeeNo("EMP001").name("张三")
                .idCard("110101199001011234").phone("13800000001")
                .department(departments.get(0)).position("高级工程师")
                .hireDate(LocalDate.of(2018, 3, 1))
                .socialSecurityBase(15000.0).housingFundBase(15000.0).socialSecurityCity("北京").active(true).build());
        employeeRepository.save(Employee.builder().employeeNo("EMP002").name("李四")
                .idCard("110101199002022345").phone("13800000002")
                .department(departments.get(0)).position("工程师")
                .hireDate(LocalDate.of(2019, 6, 15))
                .socialSecurityBase(10000.0).housingFundBase(10000.0).socialSecurityCity("北京").active(true).build());
        employeeRepository.save(Employee.builder().employeeNo("EMP003").name("王五")
                .idCard("110101199003033456").phone("13800000003")
                .department(departments.get(1)).position("销售经理")
                .hireDate(LocalDate.of(2020, 1, 1))
                .socialSecurityBase(12000.0).housingFundBase(12000.0).socialSecurityCity("上海").active(true).build());
        employeeRepository.save(Employee.builder().employeeNo("EMP004").name("赵六")
                .idCard("110101199004044567").phone("13800000004")
                .department(departments.get(2)).position("会计")
                .hireDate(LocalDate.of(2021, 9, 1))
                .socialSecurityBase(8000.0).housingFundBase(8000.0).socialSecurityCity("北京").active(true).build());
    }

    private void initSalaryItems() {
        if (salaryItemRepository.count() > 0) return;
        createItem("基本工资", "basePay", "EARNING", null, 1);
        createItem("年功工资", "seniorityPay", "EARNING", null, 2);
        createItem("效益工资", "meritPay", "EARNING", null, 3);
        createItem("岗位工资", "positionPay", "EARNING", null, 4);
        createItem("高温补贴", "heatSubsidy", "EARNING", null, 5);
        createItem("通讯补助", "communicationSubsidy", "EARNING", null, 6);
        createItem("考勤扣款", "attendanceDeduction", "DEDUCTION", null, 7);
        createItem("应发合计", "grossPay", "EARNING", "basePay+seniorityPay+meritPay+positionPay+heatSubsidy+communicationSubsidy", 8);
        createItem("养老保险", "endowmentInsurance", "SOCIAL", null, 9);
        createItem("医疗保险", "medicalInsurance", "SOCIAL", null, 10);
        createItem("失业保险", "unemploymentInsurance", "SOCIAL", null, 11);
        createItem("住房公积金", "housingFund", "FUND", null, 12);
        createItem("个人所得税", "incomeTax", "TAX", null, 13);
        createItem("实发合计", "netPay", "NET", null, 14);
    }

    private void createItem(String name, String code, String type, String formula, int order) {
        salaryItemRepository.save(SalaryItem.builder().name(name).code(code).itemType(type)
                .formula(formula).sortOrder(order).builtIn(true).active(true).build());
    }

    private void initSocialSecurity() {
        String[][] beijingData = {
                {"ENDOWMENT","8.0","16.0","3613.0","26541.0"},{"MEDICAL","2.0","8.0","5360.0","29732.0"},
                {"UNEMPLOYMENT","0.5","0.5","3613.0","26541.0"},{"INJURY","0.0","0.5","4713.0","26541.0"},
                {"MATERNITY","0.0","0.5","5360.0","29732.0"},{"HOUSING_FUND","7.0","7.0","2320.0","27786.0"},
        };
        for (String[] d : beijingData) socialSecurityRepository.save(
                SocialSecurityConfig.builder().city("北京").year(2024).insuranceType(d[0])
                        .personalRate(Double.parseDouble(d[1])).companyRate(Double.parseDouble(d[2]))
                        .baseLower(Double.parseDouble(d[3])).baseUpper(Double.parseDouble(d[4])).active(true).build());
        String[][] shanghaiData = {
                {"ENDOWMENT","8.0","16.0","5975.0","31014.0"},{"MEDICAL","2.0","8.0","5975.0","31014.0"},
                {"UNEMPLOYMENT","0.5","0.5","5975.0","31014.0"},{"INJURY","0.0","0.5","5975.0","31014.0"},
                {"MATERNITY","0.0","0.5","5975.0","31014.0"},{"HOUSING_FUND","7.0","7.0","2590.0","31014.0"},
        };
        for (String[] d : shanghaiData) socialSecurityRepository.save(
                SocialSecurityConfig.builder().city("上海").year(2024).insuranceType(d[0])
                        .personalRate(Double.parseDouble(d[1])).companyRate(Double.parseDouble(d[2]))
                        .baseLower(Double.parseDouble(d[3])).baseUpper(Double.parseDouble(d[4])).active(true).build());
    }

    private void initTaxConfig() {
        double[][] brackets = {
                {1,0,36000,3,0},{2,36000,144000,10,2520},{3,144000,300000,20,16920},
                {4,300000,420000,25,31920},{5,420000,660000,30,52920},{6,660000,960000,35,85920},{7,960000,-1,45,181920},
        };
        for (double[] b : brackets) taxConfigRepository.save(
                TaxConfig.builder().year(2024).level((int)b[0]).minIncome(b[1])
                        .maxIncome(b[2]<0?null:b[2]).taxRate(b[3]).quickDeduction(b[4]).active(true).build());
    }
}
