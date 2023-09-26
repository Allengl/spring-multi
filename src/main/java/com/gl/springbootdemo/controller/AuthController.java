package com.gl.springbootdemo.controller;

import com.gl.springbootdemo.entity.Result;
import com.gl.springbootdemo.entity.User;
import com.gl.springbootdemo.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class AuthController {
    private UserService userService;
    private AuthenticationManager authenticationManager;

    @Inject
    public AuthController(UserDetailsService userDetailsService,
                          UserService userService,
                          AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @GetMapping("/auth")
    @ResponseBody
    public Object auth() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(userName);
        if (loggedInUser == null) {
            return Result.failure("用户没有登录");
        } else {
            return Result.success("null", false, loggedInUser);
        }
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(userName);
        if (loggedInUser == null) {
            return Result.failure("用户没有登录");
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
            return Result.success("注销成功", true,null);
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, Object> usernameAndPassword) {
        String username = (String) usernameAndPassword.get("username");
        String password = (String) usernameAndPassword.get("password");

        if (username == null || password == null) {
            return Result.failure("用户名或密码为空");
        }

        if (username.length() < 1 || username.length() > 15) {
            return Result.failure("用户名长度不合法");
        }

        if (password.length() < 6 || password.length() > 16) {
            return Result.failure("密码长度不合法");
        }

        try {
            userService.save(username, password);
        } catch (DuplicateKeyException e) {
            return Result.failure("用户已存在");
        }
        return Result.success("注册成功", false, null);
    }


    @PostMapping("/auth/login")
    @ResponseBody
    public Result login(@RequestBody Map<String, Object> usernameAndPassword) {
        String username = (String) usernameAndPassword.get("username");
        String password = (String) usernameAndPassword.get("password");

        UserDetails userDetails = null;
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failure("用户不存在");
        }


        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        try {
            authenticationManager.authenticate(token);
            // 将登录状态保存在一个地方
            SecurityContextHolder.getContext().setAuthentication(token);
            return Result.success("登录成功", true, userService.getUserByUsername(username));
        } catch (BadCredentialsException e) {
            return Result.failure("密码不正确");
        }

    }

}
