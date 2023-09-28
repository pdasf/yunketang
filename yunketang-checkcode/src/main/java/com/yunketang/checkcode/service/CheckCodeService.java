package com.yunketang.checkcode.service;

import com.yunketang.checkcode.model.CheckCodeParamsDto;
import com.yunketang.checkcode.model.CheckCodeResultDto;

/**
 *  验证码接口
 */
public interface CheckCodeService {

    /**
     * 生成验证码
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     *  校验验证码
     */
    public boolean verify(String key, String code);


    /**
     *  验证码生成器
     */
    public interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);


    }

    /**
     * key生成器
     */
    public interface KeyGenerator {

        /**
         * key生成
         *
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     *  验证码存储
     */
    public interface CheckCodeStore {

        /**
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         * @return void
         * @description 向缓存设置key
         */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
