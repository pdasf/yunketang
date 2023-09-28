package com.yunketang.ucenter.service;

import com.yunketang.ucenter.model.dto.AuthParamsDto;
import com.yunketang.ucenter.model.dto.UserExt;

/**
 *  认证接口
 */
public interface AuthService {

    /**
     *  认证方法
     */
    UserExt execute(AuthParamsDto authParamsDto);

}
