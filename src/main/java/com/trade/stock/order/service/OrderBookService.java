package com.trade.stock.order.service;

import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;

import java.util.List;

/**
 * @author Prajakt Kulkarni
 * This interface provides API's to implement business logic of stock order book functionalities.
 */
public interface OrderBookService {

    OrderResponse stockOrder(TradeRequest tradeRequest) ;
    List<TradeOrderEntity> retrieveOrderBook(String stockName) throws ResourceNotFoundException;
    OrderResponse stockOrder(Long orderId, TradeRequest tradeRequest) throws ResourceNotFoundException ;
    OrderResponse deleteOrder(Long orderId) throws ResourceNotFoundException ;
}
