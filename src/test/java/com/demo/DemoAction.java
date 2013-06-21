package com.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class DemoAction {


    @RequestMapping("/index")
    public ModelAndView test(@RequestParam String key) {

        ModelAndView view = new ModelAndView("_JSON_DATA");

        key = key == null ? "sjk" : key;
        view.addObject("data", key + ", " + new Date());

        return view;
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test11(@RequestParam String key) {
        return "=====" + new Date();
    }

}
