package com.yunketang.ucenter.service;

import com.yunketang.ucenter.model.po.User;

public interface WxAuthService {
    User wxAuth(String code);
}
