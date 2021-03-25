package com.trade.stock.order.controller;

import com.trade.stock.order.constants.TradeType;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import com.trade.stock.order.service.OrderBookServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StockOrderControllerTest {

    @Mock
    private OrderBookServiceImpl orderBookService;

    @InjectMocks
    StockOrderController stockOrderController;


    @Test
    public void stockOrderCreateValidResponse()
    {
        TradeRequest request = createTradeRequest();
        when(orderBookService.stockOrder(request)).thenReturn(successResponse());
        assertEquals(successResponse(),stockOrderController.stockOrderCreate(createTradeRequest()).getBody());
    }

    @Test
    public void stockOrderCreateUpdateValidResponse() throws  ResourceNotFoundException {
        TradeRequest request = createTradeRequest();
        when(orderBookService.stockOrder(12345L,request)).thenReturn(successResponse());
        assertEquals(successResponse(),stockOrderController.stockOrderCreateUpdate(12345L,createTradeRequest()).getBody());
    }

    @Test
    public void stockOrderDeleteOrderValidResponse() throws  ResourceNotFoundException {
        TradeRequest request = createTradeRequest();
        when(orderBookService.deleteOrder(12345L)).thenReturn(successResponse());
        assertEquals(successResponse(),stockOrderController.deleteOrder(12345L).getBody());
    }

    @Test
    public void stockOrderRetrieveOrderBookValidResponse() throws  ResourceNotFoundException {
        TradeRequest request = createTradeRequest();
        when(orderBookService.retrieveOrderBook("HDFC")).thenReturn(createListTradeOrders());
        assertEquals(createListTradeOrders(),stockOrderController.retrieveOrderBook("HDFC").getBody());
    }

    @Test
    public void stockOrderCreateUpdateExceptionResponse() {
        TradeRequest request = createTradeRequest();
        try {
            when(orderBookService.stockOrder(12345L, request)).thenThrow(ResourceNotFoundException.class);
            assertEquals(failureResposne(), stockOrderController.stockOrderCreateUpdate(12345L, createTradeRequest()).getBody());
            fail("ResourceNotFoundException exception");
        }catch(Exception e)
        {
            assertTrue(true);
        }
    }

    @Test
    public void retrieveOrderBookExceptionResponse() {
        TradeRequest request = createTradeRequest();
        try {
            when(orderBookService.retrieveOrderBook("HDFC")).thenThrow(ResourceNotFoundException.class);
            assertEquals(failureResposne(), stockOrderController.retrieveOrderBook("HDFC").getBody());
            fail("ResourceNotFoundException exception");
        }catch(Exception e)
        {
            assertTrue(true);
        }
    }

    @Test
    public void deleteOrderExceptionResponse() {
        TradeRequest request = createTradeRequest();
        try {
            when(orderBookService.deleteOrder(12345L)).thenThrow(ResourceNotFoundException.class);
            assertEquals(failureResposne(), stockOrderController.deleteOrder(12345L).getBody());
            fail("ResourceNotFoundException exception");
        }catch(Exception e)
        {
            assertTrue(true);
        }
    }
    private OrderResponse successResponse() {

        OrderResponse response = new OrderResponse();
        response.setResponse("Successful execution");
        response.setOrderId(12345L);
        return response;
    }

    private OrderResponse failureResposne() {

        OrderResponse response = new OrderResponse();
        response.setResponse("Order doesn't exists for updation");
        response.setOrderId(null);
        return response;
    }

    private List<TradeOrderEntity> createListTradeOrders()
    {
        List<TradeOrderEntity> lst = new ArrayList<>();
        TradeOrderEntity entity = new TradeOrderEntity();
        entity.setOrderId(1234L);
        entity.setStockTicker("HDFC");
        entity.setTradeType(TradeType.BUY);
        entity.setQuantity(new BigInteger("50"));
        entity.setPrice(new BigDecimal("50"));
        lst.add(entity);

        return lst;
    }

    private TradeRequest createTradeRequest() {

        TradeRequest request = new TradeRequest();
        request.setStockTicker("HDFC");
        request.setQuantity(new BigInteger("50"));
        request.setTradeType(TradeType.BUY);
        request.setPrice(new BigDecimal("100"));

        return request;
    }
}
