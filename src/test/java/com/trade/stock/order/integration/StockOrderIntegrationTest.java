package com.trade.stock.order.integration;

import com.trade.stock.order.OrderApplication;
import com.trade.stock.order.constants.TradeType;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import com.trade.stock.order.service.OrderBookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = OrderApplication.class)
@AutoConfigureMockMvc
public class StockOrderIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    OrderBookService orderBookServiceMock;

    @Test
    public void givenTradeRequest_whenStockOrderCreate_thenStatus200()
            throws Exception {

        when(orderBookServiceMock.stockOrder(any())).thenReturn(successResponse());

        this.mvc.perform(post("/stockOrder/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"stockTicker\" : \"HDFC\",\n" +
                        "    \"price\" : \"100\",\n" +
                        "    \"quantity\" : \"100\",\n" +
                        "    \"tradeType\" : \"BUY\",\n" +
                        "    \"orderType\" : \"MARKET\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Successful execution"))
                .andExpect(jsonPath("$.orderId").exists());

    }

    @Test
    public void givenTradeRequest_withNegativePrice_whenStockOrderCreate_thenStatus400()
            throws Exception {

        when(orderBookServiceMock.stockOrder(any())).thenThrow(RuntimeException.class);

        this.mvc.perform(post("/stockOrder/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"stockTicker\" : \"HDFC\",\n" +
                        "    \"price\" : \"100\",\n" +
                        "    \"quantity\" : \"-50\",\n" +
                        "    \"tradeType\" : \"BUY\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenTradeRequest_whenStockOrderCreate_thenStatus500()
            throws Exception {

        when(orderBookServiceMock.stockOrder(any())).thenThrow(NullPointerException.class);

        this.mvc.perform(post("/stockOrder/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"stockTicker\" : \"HDFC\",\n" +
                        "    \"price\" : \"100\",\n" +
                        "    \"quantity\" : \"100\",\n" +
                        "    \"tradeType\" : \"BUY\",\n" +
                        "    \"orderType\" : \"MARKET\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.response").value("Exception Occurred"));

    }

    @Test
    public void givenStockTicker_whenRetrieveOrderBook_thenStatus200()
            throws Exception {

        /* setup mock */
        when(orderBookServiceMock.retrieveOrderBook("HDFC")).thenReturn(createListTradeOrders());

        this.mvc.perform(get("/stockOrder/v1/order/HDFC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].stockTicker").value("HDFC"))
                .andExpect(jsonPath("$[0].price").value(50))
                .andExpect(jsonPath("$[0].quantity").value(50))
                .andExpect(jsonPath("$[0].tradeType").value("BUY"));
    }

    @Test
    public void should_Return404_When_OrderBookNotFound() throws Exception {

        /* setup mock */
        when(orderBookServiceMock.retrieveOrderBook("HDFC")).thenThrow(ResourceNotFoundException.class);

        mvc.perform(get("/stockOrder/v1/order/HDFC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenTradeRequest_whenStockOrderUpdate_thenStatus200()
            throws Exception {

        when(orderBookServiceMock.stockOrder(any(),any())).thenReturn(successResponse());

        this.mvc.perform(put("/stockOrder/v1/order/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"stockTicker\" : \"HDFC\",\n" +
                        "    \"price\" : \"100\",\n" +
                        "    \"quantity\" : \"100\",\n" +
                        "    \"tradeType\" : \"BUY\",\n" +
                        "    \"orderType\" : \"MARKET\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Successful execution"))
                .andExpect(jsonPath("$.orderId").value(12345));

    }

    @Test
    public void givenIncorrectOrderNumber_whenStockOrderUpdate_thenStatus404()
            throws Exception {

        when(orderBookServiceMock.stockOrder(any(),any())).thenThrow(ResourceNotFoundException.class);

        this.mvc.perform(put("/stockOrder/v1/order/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"stockTicker\" : \"HDFC\",\n" +
                        "    \"price\" : \"100\",\n" +
                        "    \"quantity\" : \"100\",\n" +
                        "    \"tradeType\" : \"BUY\",\n" +
                        "    \"orderType\" : \"MARKET\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void givenOrderNumber_whenStockOrderDelete_thenStatus200()
            throws Exception {

        when(orderBookServiceMock.deleteOrder(12345L)).thenReturn(successResponse());

        this.mvc.perform(delete("/stockOrder/v1/order/12345")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Successful execution"));

    }

    @Test
    public void should_Return404_When_OrderNotFoundForDeletion() throws Exception {

        /* setup mock */
        when(orderBookServiceMock.deleteOrder(12345L)).thenThrow(ResourceNotFoundException.class);

        mvc.perform(delete("/stockOrder/v1/order/12345")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private OrderResponse successResponse() {

        OrderResponse response = new OrderResponse();
        response.setResponse("Successful execution");
        response.setOrderId(12345L);
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
