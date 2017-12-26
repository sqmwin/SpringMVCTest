package com.sqm.servlet.handler;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * <p>
 *
 * @author sqm
 * @version 1.0
 */
public class MyController implements Controller {
    @Override
    public ModelAndView handleRequest(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("welcome", "hello spring mvc world");
        modelAndView.setViewName("/WEB-INF/jsp/welcome.jsp");
        modelAndView.setViewName("welcome");
        return modelAndView;
        //return new ModelAndView("/index.jsp");
    }
}
