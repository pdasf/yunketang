package com.yunketang.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunketang.ucenter.feignclient.CheckCodeClient;
import com.yunketang.ucenter.mapper.UserMapper;
import com.yunketang.ucenter.model.dto.AuthParamsDto;
import com.yunketang.ucenter.model.dto.UserExt;
import com.yunketang.ucenter.model.po.User;
import com.yunketang.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @description 账号密码认证
 */
@Slf4j
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    UserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public UserExt execute(AuthParamsDto authParamsDto) {
        // 校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)) {
            throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        // 1. 获取账号
        String username = authParamsDto.getUsername();
        // 2. 根据账号去数据库中查询是否存在
        User xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        // 3. 不存在抛异常
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        // 4. 校验密码
        // 4.1 获取用户输入的密码
        String passwordForm = authParamsDto.getPassword();
        // 4.2 获取数据库中存储的密码
        String passwordDb = xcUser.getPassword();
        // 4.3 比较密码
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        // 4.4 不匹配，抛异常
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        // 4.5 匹配，封装返回
        UserExt xcUserExt = new UserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
