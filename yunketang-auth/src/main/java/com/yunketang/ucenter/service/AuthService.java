package com.yunketang.ucenter.service;

import com.yunketang.ucenter.model.dto.AuthParamsDto;
import com.yunketang.ucenter.model.dto.UserExt;

/**
 * @description 认证接口
 */
public interface AuthService {

    /**
     * @param authParamsDto 认证参数
     * @return com.yunketang.ucenter.model.po.User 用户信息
     * @description 认证方法
     */
    UserExt execute(AuthParamsDto authParamsDto);

}
