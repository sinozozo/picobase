package com.picobase.console.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ConsoleController {

    @GetMapping("/pbLogin")
    public String toLogin() {
        return "redirect:/console/index.html";
    }
}
