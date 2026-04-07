package com.campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/index")
    public String indexPage() {
        return "admin/index";
    }
}
