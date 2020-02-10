package com.demo.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: wuquan
 * @Date: 2020-02-10 10:30
 */
@MyController
@MyRequestMapping("/index")
public class IndexController {

    @MyRequestMapping("/home")
    public String index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("hello");
      return "hello index";
    }
}
