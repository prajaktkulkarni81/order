package com.trade.stock.order.controller;

import com.trade.stock.order.api.StockOrderApi;
import static com.trade.stock.order.constants.Constants.BASEPATH;

import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import com.trade.stock.order.service.OrderBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Prajakt Kulkarni
 *
 * This controller provides implementation of stock order book creation, updation, deletion and retrival functionalities.
 */
@Controller
@Slf4j
@RequestMapping(value=BASEPATH)
public class StockOrderController implements StockOrderApi {

    final OrderBookService orderbookService;

    public StockOrderController(OrderBookService orderbookService) {
        this.orderbookService = orderbookService;
    }

    @Override
    public ResponseEntity<OrderResponse> stockOrderCreate(TradeRequest tradeRequest) {
        log.info("Stock order creation : START");
        return new ResponseEntity<>(orderbookService.stockOrder(tradeRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderResponse> stockOrderCreateUpdate(Long orderId, TradeRequest tradeRequest) throws ResourceNotFoundException {
        log.info("Stock order updation : START");
        return new ResponseEntity<>(orderbookService.stockOrder(orderId, tradeRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TradeOrderEntity>> retrieveOrderBook(String stockName) throws ResourceNotFoundException {
        log.info("Stock order book retrieval : START");
        return new ResponseEntity<>(orderbookService.retrieveOrderBook(stockName),HttpStatus.OK);

    }

    @Override
    public ResponseEntity<OrderResponse> deleteOrder(Long orderId) throws ResourceNotFoundException  {
        log.info("Stock order deletion : START");
        return new ResponseEntity<>(orderbookService.deleteOrder(orderId), HttpStatus.OK);
    }
}
