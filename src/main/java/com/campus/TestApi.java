package com.campus;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestApi {
    public static void main(String[] args) {
        try {
            System.out.println("正在启动Spring应用...");
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"applicationContext.xml", "springmvc.xml"}
            );
            System.out.println("Spring应用启动成功！");
            System.out.println("等待3秒后关闭...");
            Thread.sleep(3000);
            context.close();
        } catch (Exception e) {
            System.out.println("启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}