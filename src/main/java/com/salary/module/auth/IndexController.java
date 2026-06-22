package com.salary.module.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "<html><body style='font-family: SimSun, sans-serif; padding: 30px;'>"
            + "<h2>工资管理系统</h2>"
            + "<p>系统已启动成功！</p>"
            + "<hr/>"
            + "<h4>API 接口测试：</h4>"
            + "<ul>"
            + "<li><b>登录测试：</b><br/>"
            + "POST <code>/api/auth/login</code><br/>"
            + "Body: {\"username\":\"admin\",\"password\":\"admin123\"}</li>"
            + "<li style='margin-top:10px;'><b>H2 数据库控制台：</b><br/>"
            + "<a href='/h2-console'>/h2-console</a> (JDBC URL: jdbc:h2:mem:salary_db)</li>"
            + "</ul>"
            + "<hr/>"
            + "<h4>预置测试账号：</h4>"
            + "<table border='1' cellpadding='8' style='border-collapse:collapse;'>"
            + "<tr><th>角色</th><th>用户名</th><th>密码</th></tr>"
            + "<tr><td>管理员</td><td>admin</td><td>admin123</td></tr>"
            + "<tr><td>财务</td><td>finance</td><td>finance123</td></tr>"
            + "<tr><td>人事</td><td>hr</td><td>hr123</td></tr>"
            + "<tr><td>员工</td><td>employee1</td><td>emp123</td></tr>"
            + "</table>"
            + "</body></html>";
    }
}
