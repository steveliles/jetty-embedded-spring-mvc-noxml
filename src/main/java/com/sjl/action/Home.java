package com.sjl.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
@RequestMapping
public class Home {

    @RequestMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("index");
    }

    @RequestMapping("/test.do")
    public ModelAndView test(@RequestParam String key) {

        ModelAndView view = new ModelAndView("_JSON_DATA");
        view.addObject("data", key + ", " + new Date());

        return view;
    }

}
