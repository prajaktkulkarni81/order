package com.trade.stock.order.model;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class TradeRequest {

    @NotBlank(message = "Stock Ticker is mandatory")
    String stockTicker;
    BigDecimal price;
    @NotNull(message = "Quantity is mandatory")
    @Positive
    BigInteger quantity;
    TradeType tradeType;
    OrderType orderType;
}
