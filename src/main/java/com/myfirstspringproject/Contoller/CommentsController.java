package com.myfirstspringproject.Contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CommentsController {

    @RequestMapping({"/", "/comments"})
    public String homeMapping(){
        return "/index";
    }
}
