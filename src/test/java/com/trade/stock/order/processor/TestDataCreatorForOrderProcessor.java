package com.trade.stock.order.processor;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.processor.OrderProcessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public final class TestDataCreatorForOrderProcessor {


    static void emptyOrders() {

        OrderProcessor.buyMap.clear();
        OrderProcessor.sellMap.clear();
    }

    static void prepareOneBuyOrder() {
        OrderProcessor.buyMap.put("HDFC",createListTradeOrdersForBuy());
    }

    static void prepareOneSellOrder() {
        OrderProcessor.sellMap.put("HDFC",createListTradeOrdersForSell());
    }

    static TradeOrderEntity createTradeOrderEntity(Long orderId, String stockTicker, TradeType tradeType, BigInteger quantity, BigDecimal price, OrderType orderType,Long timeStamp)
    {
        TradeOrderEntity entity = new TradeOrderEntity();
        entity.setOrderId(orderId);
        entity.setStockTicker(stockTicker);
        entity.setTradeType(tradeType);
        entity.setQuantity(quantity);
        entity.setPrice(price);
        entity.setOrderType(orderType);
        entity.setTradeTime(timeStamp);
        return entity;
    }

    static PriorityBlockingQueue<TradeOrderEntity> createListTradeOrdersForBuy()
    {
        PriorityBlockingQueue<TradeOrderEntity> lst = new PriorityBlockingQueue<TradeOrderEntity>(10 , getComparator(TradeType.BUY));
        TradeOrderEntity entity = new TradeOrderEntity();
        entity.setOrderId(1234L);
        entity.setStockTicker("HDFC");
        entity.setTradeType(TradeType.BUY);
        entity.setQuantity(new BigInteger("50"));
        entity.setPrice(new BigDecimal("50"));
        entity.setOrderType(OrderType.LIMIT);
        lst.offer(entity);

        return lst;
    }

    private static PriorityBlockingQueue<TradeOrderEntity> createListTradeOrdersForSell()
    {
        PriorityBlockingQueue<TradeOrderEntity> lst = new PriorityBlockingQueue<TradeOrderEntity>(10 , getComparator(TradeType.BUY));
        TradeOrderEntity entity = new TradeOrderEntity();
        entity.setOrderId(1234L);
        entity.setStockTicker("HDFC");
        entity.setTradeType(TradeType.SELL);
        entity.setQuantity(new BigInteger("100"));
        entity.setPrice(new BigDecimal("50"));
        entity.setOrderType(OrderType.LIMIT);
        lst.offer(entity);

        return lst;
    }

    private static Comparator<TradeOrderEntity> getComparator(TradeType tradeType) {
        Comparator<TradeOrderEntity> compareByPriceTimeOrders = tradeType == TradeType.BUY ?
                Comparator.comparing(TradeOrderEntity::getOrderType)
                .thenComparing(TradeOrderEntity::getPrice)
                .thenComparing(TradeOrderEntity::getTradeTime) :  Comparator
                .comparing(TradeOrderEntity::getOrderType)
                .thenComparing(TradeOrderEntity::getPrice)
                .thenComparing(TradeOrderEntity::getTradeTime).reversed();

        return compareByPriceTimeOrders;
    }
}
