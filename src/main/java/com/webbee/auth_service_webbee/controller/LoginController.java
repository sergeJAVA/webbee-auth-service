package com.webbee.auth_service_webbee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/loginForm")
    public String loginPage() {
        return "login";
    }

    @ResponseBody
    @GetMapping("/success-page")
    public String success() {
        return "Welcome";
    }

    @ResponseBody
    @GetMapping("/")
    public String home() {
        return "Home";
    }

}
