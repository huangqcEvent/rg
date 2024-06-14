package com.hqc.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.hqc.reggie.common.BaseContext;
import com.hqc.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*检查用户是否已经登陆成功过滤器*/
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求的uri
        String requestURI=request.getRequestURI();
        log.info("拦截到请求:{}",requestURI);
        //定于不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2.判断本次请求是否需要处理，检查登录状态
        boolean check = check(urls,requestURI);

        //3.如果不需要处理，则之间放行
        if (check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.判断登录状态，如果已经登录，则放行
       if(request.getSession().getAttribute("employee")!=null){
           log.info("用户已经登录，用户id为{}",request.getSession().getAttribute("employee"));

           //在当前线程中设置id，同线程的其他需要的可以取
           Long empId=(Long) request.getSession().getAttribute("employee");
           BaseContext.setCurrentId(empId);
           //获得当前线程id
           long id = Thread.currentThread().getId();
           log.info("当前线程id为{}",id);

           filterChain.doFilter(request,response);
           return;
       }
        //4-2.判断登录状态，如果已经登录，则放行

        //④. 判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user")!=null){
            log.info("用户{}已登录",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

       //5.如果为登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录！！！！");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }


    //路径匹配方法，检查本次请求是否要放行
    public boolean check(String[] urls,String requestURI){
        for (String url:urls){
           boolean match = PATH_MATCHER.match(url, requestURI);
           if (match){
               return true;
           }
        }
        return false;
    }
}

