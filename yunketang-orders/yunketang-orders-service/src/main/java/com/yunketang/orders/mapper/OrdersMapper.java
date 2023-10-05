package com.yunketang.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunketang.orders.model.po.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}
