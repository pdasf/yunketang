package com.yunketang.auth.controller;

import com.yunketang.ucenter.model.po.User;
import com.yunketang.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Slf4j
@Controller
public class WxLoginController {
    @Autowired
    WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        User user = wxAuthService.wxAuth(code);
        if (user == null) {
            return "redirect:http://localhost/error.html";
        }
        String username = user.getUsername();
        return "redirect:http://localhost/sign.html?username=" + username + "&authType=wx";
    }
}
