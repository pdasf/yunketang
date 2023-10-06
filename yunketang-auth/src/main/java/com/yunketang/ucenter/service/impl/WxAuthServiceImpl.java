package com.yunketang.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunketang.ucenter.mapper.UserMapper;
import com.yunketang.ucenter.mapper.UserRoleMapper;
import com.yunketang.ucenter.model.po.UserRole;
import com.yunketang.ucenter.model.dto.AuthParamsDto;
import com.yunketang.ucenter.model.dto.UserExt;
import com.yunketang.ucenter.model.po.User;
import com.yunketang.ucenter.service.AuthService;
import com.yunketang.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserRoleMapper userRoleMapper;

    @Autowired
    WxAuthServiceImpl wxAuthService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    @Override
    public User wxAuth(String code) {
        // 1. 获取access_token
        Map<String, String> access_token_map = getAccess_token(code);
        String accessToken = access_token_map.get("access_token");

        // 2. 获取用户信息
        String openid = access_token_map.get("openid");
        Map<String, String> user_info_map = getUserInfo(accessToken, openid);

        // 3. 添加用户信息到数据库
        User xcUser = wxAuthService.addWxUser(user_info_map);
        return xcUser;
    }

    /**
     * 微信扫码认证，不需要校验密码和验证码
     *
     * @param authParamsDto 认证参数
     */
    @Override
    public UserExt execute(AuthParamsDto authParamsDto) {
        // 账号
        String username = authParamsDto.getUsername();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        UserExt xcUserExt = new UserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }

    private Map<String, String> getAccess_token(String code) {
        // 1. 请求路径模板，参数用%s占位符
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 2. 填充占位符：appid，secret，code
        String url = String.format(url_template, appid, secret, code);
        // 3. 远程调用URL，POST方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 4. 获取响应结果，响应结果为json格式
        String result = exchange.getBody();
        // 5. 转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    private Map<String, String> getUserInfo(String access_token, String openid) {
        // 1. 请求路径模板，参数用%s占位符
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        // 2. 填充占位符，access_token和openid
        String url = String.format(url_template, access_token, openid);
        // 3. 远程调用URL，GET方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // 4. 获取响应结果，JSON格式
        String result = exchange.getBody();
        // 4.1 需要转码
        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 5. 转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    @Transactional

    public User addWxUser(Map<String, String> user_info_map) {
        // 1. 获取用户唯一标识：unionid作为用户的唯一表示
        String unionid = user_info_map.get("unionid");
        // 2. 根据唯一标识，判断数据库是否存在该用户
        User xcUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getWxUnionid, unionid));
        // 2.1 存在，则直接返回
        if (xcUser != null) {
            return xcUser;
        }
        // 2.2 不存在，新增
        xcUser = new User();
        // 2.3 设置主键
        String uuid = UUID.randomUUID().toString();
        xcUser.setId(uuid);
        // 2.4 设置其他数据库非空约束的属性
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(user_info_map.get("nickname"));
        xcUser.setUserpic(user_info_map.get("headimgurl"));
        xcUser.setName(user_info_map.get("nickname"));
        xcUser.setUtype("101001");  // 学生类型
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        // 2.5 添加到数据库
        userMapper.insert(xcUser);
        // 3. 添加用户信息到用户角色表
        UserRole xcUserRole = new UserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(xcUserRole);
        return xcUser;
    }
}