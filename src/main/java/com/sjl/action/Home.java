package com.sjl.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class Home {

    @RequestMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("abc/abc");
    }

    @RequestMapping("/test.do")
    public ModelAndView test(@RequestParam String key) {

        ModelAndView view = new ModelAndView("_JSON_DATA");
        view.addObject("data", key + ", " + new Date());

        return view;
    }

    @RequestMapping("/test11")
    @ResponseBody
    public String test11(@RequestParam String key) {
        return "=====" + new Date();
    }

}
