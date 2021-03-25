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

    @EqualsAndHashCode.Exclude
    Long orderId;
    String stockTicker;
    Long tradeTime;
    BigDecimal price;
    BigInteger quantity;
    TradeType tradeType;
    @EqualsAndHashCode.Exclude
    OrderType orderType;

}
