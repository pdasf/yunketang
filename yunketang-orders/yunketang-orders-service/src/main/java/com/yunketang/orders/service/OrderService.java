package com.yunketang.orders.service;

import com.yunketang.messagesdk.model.po.MqMessage;
import com.yunketang.orders.model.dto.AddOrderDto;
import com.yunketang.orders.model.dto.PayRecordDto;
import com.yunketang.orders.model.dto.PayStatusDto;
import com.yunketang.orders.model.po.PayRecord;

public interface OrderService {
    /**
     * 创建商品订单
     *
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return 支付交易记录
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付交易记录
     *
     * @param payNo 交易记录号
     * @return
     */
    PayRecord getPayRecordByPayNo(String payNo);

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);

    void saveAlipayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     *
     * @param mqMessage 消息
     */
    void notifyPayResult(MqMessage mqMessage);
}
