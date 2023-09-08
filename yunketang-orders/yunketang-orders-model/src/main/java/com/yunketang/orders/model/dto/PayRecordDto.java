package com.yunketang.orders.model.dto;

import com.yunketang.orders.model.po.PayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @description 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends PayRecord {

    //二维码
    private String qrcode;

}
