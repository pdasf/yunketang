package com.yunketang.ucenter.service;

import com.yunketang.ucenter.model.dto.FindPswDto;
import com.yunketang.ucenter.model.dto.RegisterDto;

public interface VerifyService {
    void findPassword(FindPswDto findPswDto);

    void register(RegisterDto registerDto);
}
