package com.yunketang.orders.mapper;

import com.yunketang.orders.model.po.OrdersGoods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersGoodsMapper{
    int insert(OrdersGoods goods);
}
