package com.trade.stock.order.service;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import com.trade.stock.order.processor.OrderProcessor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderBookServiceImplTest {

    @Mock
    OrderProcessor processor;

    @InjectMocks
    OrderBookServiceImpl orderBookService;

    @Test
    public void stockOrderCreateValidResponse()
    {
        assertEquals(successResponse().getResponse(),orderBookService.stockOrder(createTradeRequest()).getResponse());
    }

    @Test
    public void retrieveOrderBookValidResponse() throws ResourceNotFoundException
    {
        when(processor.retrieveOrderBook("HDFC")).thenReturn(createListTradeOrders());
        assertEquals(createListTradeOrders(),orderBookService.retrieveOrderBook("HDFC"));
    }

    @Test
    public void retrieveOrderBookExceptionResponse() throws ResourceNotFoundException
    {
        when(processor.retrieveOrderBook("HDFC")).thenThrow(ResourceNotFoundException.class);
        try {
            assertEquals(createListTradeOrders(), orderBookService.retrieveOrderBook("HDFC"));
            fail();
        }catch (ResourceNotFoundException e)
        {
            assertTrue(true);
        }

    }

    @Test
    public void stockOrderUpdateValidResponse() throws ResourceNotFoundException
    {
        assertEquals(successResponse(),orderBookService.stockOrder(12345L,createTradeRequest()));
    }


    @Test
    public void stockOrderUpdateExceptionResponse() throws ResourceNotFoundException
    {
        TradeOrderEntity entity = new TradeOrderEntity();
        doThrow(new ResourceNotFoundException()).when(processor).processUpdate(any());
        try {
            assertEquals(successResponse(), orderBookService.stockOrder(12345L,createTradeRequest()));
            fail();
        }catch (ResourceNotFoundException e)
        {
            assertTrue(true);
        }

    }
    @Test
    public void deleteOrderValidResponse() throws ResourceNotFoundException
    {
        when(processor.deleteOrder(12345L)).thenReturn(successResponse());
        assertEquals(successResponse(),orderBookService.deleteOrder(12345L));
    }
    @Test
    public void deleteOrderExceptionResponse() throws ResourceNotFoundException
    {
        when(processor.deleteOrder(12345L)).thenThrow(ResourceNotFoundException.class);
        try {
            assertEquals(successResponse(), orderBookService.deleteOrder(12345L));
            fail();
        }catch (ResourceNotFoundException e)
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

    private TradeRequest createTradeRequest() {

        TradeRequest request = new TradeRequest();
        request.setStockTicker("HDFC");
        request.setQuantity(new BigInteger("50"));
        request.setTradeType(TradeType.BUY);
        request.setPrice(new BigDecimal("100"));
        request.setOrderType(OrderType.LIMIT);
        return request;
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
        entity.setOrderType(OrderType.LIMIT);
        lst.add(entity);

        return lst;
    }
}
