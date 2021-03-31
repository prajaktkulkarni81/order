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
    private String stockTicker;
    private BigDecimal price;
    @NotNull(message = "Quantity is mandatory")
    @Positive
    private BigInteger quantity;
    @NotNull(message = "Trade Type is mandatory")
    private TradeType tradeType;
    @NotNull(message = "Order Type is mandatory")
    private OrderType orderType;
}
