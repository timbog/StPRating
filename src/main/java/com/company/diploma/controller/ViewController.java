package com.company.diploma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

    @RequestMapping(value = "/stats")
    public ModelMap startPage() {
        ModelMap modelMap = new ModelMap("stats");
        return modelMap;
    }
}
