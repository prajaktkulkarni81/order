package com.trade.stock.order.service;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import com.trade.stock.order.processor.OrderProcessor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;
import java.util.Random;

/**
 * @author Prajakt Kulkarni
 * Stock order processor service
 */
@Service
@Slf4j
public class OrderBookServiceImpl implements OrderBookService {

    final OrderProcessor processor;

    public OrderBookServiceImpl(OrderProcessor processor) {
        this.processor = processor;
    }


    @Override
    public OrderResponse stockOrder(TradeRequest tradeRequest) {
        log.info("Stock order create : START");
        OrderResponse response = new OrderResponse();
        TradeOrderEntity tradeOrderEntity = createTradeOrder(tradeRequest);
        processor.process(tradeOrderEntity);
        response.setOrderId(tradeOrderEntity.getOrderId());
        response.setResponse("Successful execution");
        log.info("Stock order create : END");
        return response;
    }

    @Override
    public List<TradeOrderEntity> retrieveOrderBook(String stockName) throws ResourceNotFoundException {
        log.info("Stock order retrieve : START");
        return processor.retrieveOrderBook(stockName);
    }

    @Override
    public OrderResponse stockOrder(Long orderId, TradeRequest tradeRequest) throws ResourceNotFoundException {
        log.info("Stock order updation : START");
        OrderResponse response = new OrderResponse();
        TradeOrderEntity tradeOrderEntity = createTradeOrder(orderId, tradeRequest);
        processor.processUpdate(tradeOrderEntity);
        response.setOrderId(tradeOrderEntity.getOrderId());
        response.setResponse("Successful execution");
        log.info("Stock order updation : END");
        return response;
    }

    @Override
    public OrderResponse deleteOrder(Long orderId) throws ResourceNotFoundException {
        log.info("Stock order deletion : START");
        return processor.deleteOrder(orderId);
    }


    private TradeOrderEntity createTradeOrder(TradeRequest tradeRequest) {
        return createTradeOrder(null, tradeRequest);
    }

    private TradeOrderEntity createTradeOrder(Long orderId, TradeRequest tradeRequest){
        TradeOrderEntity order = new TradeOrderEntity();
        Random random = new  Random();
        if(orderId != null)
        {
            order.setOrderId(orderId);
        }else {
            order.setOrderId(random.nextLong());
        }
        order.setQuantity(tradeRequest.getQuantity());
        order.setTradeTime(System.currentTimeMillis());
        order.setTradeType(tradeRequest.getTradeType());
        order.setStockTicker(tradeRequest.getStockTicker());
        order.setOrderType(tradeRequest.getOrderType());
        if(tradeRequest.getOrderType() == OrderType.MARKET)
        {
            //This is to ignore price for market orders.
            order.setPrice(new BigDecimal("-1"));
        }else
        {
            order.setPrice(tradeRequest.getPrice());
        }
        return order;
    }

}
