package com.yunketang.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunketang.base.exception.YunketangException;
import com.yunketang.base.utils.IdWorkerUtils;
import com.yunketang.base.utils.QRCodeUtil;
import com.yunketang.messagesdk.model.po.MqMessage;
import com.yunketang.messagesdk.service.MqMessageService;
import com.yunketang.orders.config.AlipayConfig;
import com.yunketang.orders.config.PayNotifyConfig;
import com.yunketang.orders.mapper.OrdersGoodsMapper;
import com.yunketang.orders.mapper.OrdersMapper;
import com.yunketang.orders.mapper.PayRecordMapper;
import com.yunketang.orders.model.dto.AddOrderDto;
import com.yunketang.orders.model.dto.PayRecordDto;
import com.yunketang.orders.model.dto.PayStatusDto;
import com.yunketang.orders.model.po.Orders;
import com.yunketang.orders.model.po.OrdersGoods;
import com.yunketang.orders.model.po.PayRecord;
import com.yunketang.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Autowired
    OrdersMapper xcOrdersMapper;

    @Autowired
    PayRecordMapper xcPayRecordMapper;

    @Autowired
    OrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 1. 添加商品订单
        Orders orders = saveOrders(userId, addOrderDto);

        // 2. 添加支付交易记录
        PayRecord payRecord = createPayRecord(orders);
        // 3. 生成二维码
        String qrCode = null;
        try {
            qrcodeurl = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(qrcodeurl, 200, 200);
        } catch (IOException e) {
            YunketangException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public PayRecord getPayRecordByPayNo(String payNo) {
        return xcPayRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {

        // 1. 调用支付宝接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);

        // 2. 拿到支付结果，更新支付记录表和订单表的状态为 已支付
        saveAlipayStatus(payStatusDto);

        return null;
    }

    /**
     * 调用支付宝接口查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {

        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                YunketangException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            YunketangException.cast("请求支付查询查询失败");
        }

        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;
    }

    /**
     * 保存支付结果信息
     *
     * @param payStatusDto 支付结果信息
     */
    public void saveAlipayStatus(PayStatusDto payStatusDto) {
        // 1. 获取支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        // 2. 查询数据库订单状态
        PayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            YunketangException.cast("未找到支付记录");
        }
        Orders order = xcOrdersMapper.selectById(payRecord.getOrderId());
        if (order == null) {
            YunketangException.cast("找不到相关联的订单");
        }
        String statusFromDB = payRecord.getStatus();
        // 2.1 已支付，直接返回
        if ("600002".equals(statusFromDB)) {
            return;
        }
        // 3. 查询支付宝交易状态
        String tradeStatus = payStatusDto.getTrade_status();
        // 3.1 支付宝交易已成功，保存订单表和交易记录表，更新交易状态
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 更新支付交易表
            payRecord.setStatus("601002");
            payRecord.setOutPayNo(payStatusDto.getTrade_no());
            payRecord.setOutPayChannel("Alipay");
            payRecord.setPaySuccessTime(LocalDateTime.now());
            int updateRecord = xcPayRecordMapper.updateById(payRecord);
            if (updateRecord <= 0) {
                YunketangException.cast("更新支付交易表失败");
            }
            // 更新订单表
            order.setStatus("600002");
            int updateOrder = xcOrdersMapper.updateById(order);
            if (updateOrder <= 0) {
                log.debug("更新订单表失败");
                YunketangException.cast("更新订单表失败");
            }
        }
        // 4. 保存消息记录，参数1：支付结果类型通知；参数2：业务id；参数3：业务类型
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", order.getOutBusinessId(), order.getOrderType(), null);
        // 5. 通知消息
        notifyPayResult(mqMessage);
    }

    @Override
    public void notifyPayResult(MqMessage mqMessage) {
        // 1. 将消息体转为Json
        String jsonMsg = JSON.toJSONString(mqMessage);
        // 2. 设消息的持久化方式为PERSISTENT，即消息会被持久化到磁盘上，确保即使在RabbitMQ服务器重启后也能够恢复消息。
        Message msgObj = MessageBuilder.withBody(jsonMsg.getBytes()).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
        // 3. 封装CorrelationData，
        CorrelationData correlationData = new CorrelationData(mqMessage.getId().toString());
        correlationData.getFuture().addCallback(result -> {
            if (result.isAck()) {
                // 3.1 消息发送成功，删除消息表中的记录
                log.debug("消息发送成功：{}", jsonMsg);
                mqMessageService.completed(mqMessage.getId());
            } else {
                // 3.2 消息发送失败
                log.error("消息发送失败，id：{}，原因：{}", mqMessage.getId(), result.getReason());
            }
        }, ex -> {
            // 3.3 消息异常
            log.error("消息发送异常，id：{}，原因：{}", mqMessage.getId(), ex.getMessage());
        });
        // 4. 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

    /**
     * 保存订单信息，保存订单表和订单明细表，需要做幂等性判断
     *
     * @param userId      用户id
     * @param addOrderDto 选课信息
     * @return
     */
    @Transactional
    public Orders saveOrders(String userId, AddOrderDto addOrderDto) {
        // 1. 幂等性判断
        Orders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        // 2. 插入订单表
        order = new Orders();
        BeanUtils.copyProperties(addOrderDto, order);
        order.setId(IdWorkerUtils.getInstance().nextId());
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(userId);
        order.setStatus("600001");
        int insert = xcOrdersMapper.insert(order);
        if (insert <= 0) {
            YunketangException.cast("插入订单记录失败");
        }
        // 3. 插入订单明细表
        Long orderId = order.getId();
        String orderDetail = addOrderDto.getOrderDetail();
        List<OrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetail, OrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            goods.setOrderId(orderId);
            int insert1 = xcOrdersGoodsMapper.insert(goods);
            if (insert1 <= 0) {
                YunketangException.cast("插入订单明细失败");
            }
        });
        return order;
    }

    /**
     * 根据业务id查询订单
     *
     * @param businessId 业务id是选课记录表中的主键
     * @return
     */
    public Orders getOrderByBusinessId(String businessId) {
        return xcOrdersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOutBusinessId, businessId));
    }

    public PayRecord createPayRecord(Orders orders) {
        if (orders == null) {
            YunketangException.cast("订单不存在");
        }
        if ("600002".equals(orders.getStatus())) {
            YunketangException.cast("订单已支付");
        }
        PayRecord payRecord = new PayRecord();
        payRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");
        payRecord.setUserId(orders.getUserId());
        int insert = xcPayRecordMapper.insert(payRecord);
        if (insert <= 0) {
            YunketangException.cast("插入支付交易记录失败");
        }
        return payRecord;
    }

}