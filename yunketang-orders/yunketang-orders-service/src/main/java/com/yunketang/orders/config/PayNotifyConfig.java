package com.yunketang.orders.config;

import com.alibaba.fastjson.JSON;
import com.yunketang.messagesdk.model.po.MqMessage;
import com.yunketang.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class PayNotifyConfig implements ApplicationContextAware {

    //交换机
    public static final String PAYNOTIFY_EXCHANGE_FANOUT = "paynotify_exchange_fanout";

    /**
     * 设置消息发送到mq失败时回调功能
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 消息处理service
        MqMessageService mqMessageService = applicationContext.getBean(MqMessageService.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // 投递失败，记录日志
            log.info("消息发送失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message);
            MqMessage mqMessage = JSON.parseObject(message.toString(), MqMessage.class);
            //将消息再添加到消息表
            mqMessageService.addMessage(mqMessage.getMessageType(), mqMessage.getBusinessKey1(),
                    mqMessage.getBusinessKey2(), mqMessage.getBusinessKey3());
        });
    }
}
