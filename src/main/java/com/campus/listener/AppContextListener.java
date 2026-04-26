package com.campus.listener;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * 应用关闭时清理资源,防止内存泄漏
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 应用启动时无需操作
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== 开始清理应用资源 ===");

        // 1. 关闭 Druid 数据源
        try {
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
            if (context != null) {
                DruidDataSource dataSource = context.getBean(DruidDataSource.class);
                if (dataSource != null) {
                    dataSource.close();
                    System.out.println("Druid 数据源已关闭");
                }
            }
        } catch (Exception e) {
            System.err.println("关闭 Druid 数据源时出错: " + e.getMessage());
        }

        // 2. 注销 JDBC 驱动
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("JDBC 驱动已注销: " + driver.getClass().getName());
            } catch (SQLException e) {
                System.err.println("注销 JDBC 驱动失败: " + e.getMessage());
            }
        }

        // 3. 清理线程上下文类加载器
        try {
            Thread.currentThread().setContextClassLoader(null);
        } catch (Exception e) {
            System.err.println("清理线程上下文类加载器失败: " + e.getMessage());
        }

        System.out.println("=== 应用资源清理完成 ===");
    }
}
