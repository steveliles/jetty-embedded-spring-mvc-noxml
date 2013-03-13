package com.sjl.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Home {

    @RequestMapping("/")
    public ModelAndView home() {
        return new ModelAndView("index");
    }

}
