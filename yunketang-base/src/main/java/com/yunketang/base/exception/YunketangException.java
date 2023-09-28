package com.yunketang.base.exception;


/**
 *  学成在线项目异常类
 */
public class YunketangException extends RuntimeException {
    private String errMessage;

    public String getErrMessage() {
        return errMessage;
    }

    public YunketangException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new YunketangException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new YunketangException(errMessage);
    }
}
