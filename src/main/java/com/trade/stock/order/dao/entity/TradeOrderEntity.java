package com.trade.stock.order.dao.entity;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@EqualsAndHashCode
public class TradeOrderEntity implements Serializable {

    private Long orderId;
    private String stockTicker;
    private Long tradeTime;
    private BigDecimal price;
    private BigInteger quantity;
    private TradeType tradeType;
    private OrderType orderType;

}
