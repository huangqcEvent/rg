package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hqc.reggie.common.R;
import com.hqc.reggie.pojo.User;
import com.hqc.reggie.service.UserService;
import com.hqc.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
//登录时输入手机号 发送验证码登录
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user , HttpSession session){
        String phone = user.getPhone();
        //获取手机号 判断手机号是否正常
        if (StringUtils.isNotEmpty(phone)){
            //手机号正常，发送验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //将手机号和验证码存储到session中
            session.setAttribute(phone,code);//存的式键值对
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }
//用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());

        //获取用户输入的手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //通过手机号获取session中村的验证码
        Object codeInSession = session.getAttribute(phone);
//判断是否存在此手机号对应的验证码，如果存在是否正确
        if (codeInSession!=null&&codeInSession.equals(code)){
            //如果存在且正确，查找对应数据库中是否存在此用户，如果不存在，就创建。
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //登录成功，将用户id存入session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("验证码错误");
    }
//退出 将session中的id清除
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        Object user = session.getAttribute("user");
        User user1 = userService.getById(user.toString());
        String phone = user1.getPhone();
        session.removeAttribute("user");
        session.removeAttribute(phone);
        return R.success("退出成功");
    }
}
