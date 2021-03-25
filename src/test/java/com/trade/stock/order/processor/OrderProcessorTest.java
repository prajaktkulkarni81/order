package com.trade.stock.order.processor;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;

import com.trade.stock.order.exception.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OrderProcessorTest {

    OrderProcessor processor = new OrderProcessor();

    @Before
    public void cleanData()
    {
        TestDataCreatorForOrderProcessor.emptyOrders();
    }

    @After
    public void cleanDataAfterRun()
    {
        TestDataCreatorForOrderProcessor.emptyOrders();
    }

    @Test
    public void processValidResponseWithZeroExistingBuyOrders()
    {

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis())));
    }

    @Test
    public void processValidResponseWithOneExistingBuyOrders()
    {
        TestDataCreatorForOrderProcessor.prepareOneBuyOrder();
        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(1, OrderProcessor.buyMap.size());
        assertEquals(2, OrderProcessor.buyMap.get("HDFC").size());
    }

    @Test
    public void processValidResponseWithOneExistingBuyAndSellOrders()
    {
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis()));
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.SELL,new BigInteger("20"), BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(0, OrderProcessor.buyMap.get("HDFC").size());
        assertEquals(0, OrderProcessor.sellMap.get("HDFC").size());
    }

    @Test
    public void test_PriceTime_Validation_With2SameBuy_OneSell()
    {
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis()));
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(34567L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(76898L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(1, OrderProcessor.buyMap.get("HDFC").size());
        assertEquals(0, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
        assertEquals(34567L, OrderProcessor.buyMap.get("HDFC").peek().getOrderId());
    }

    @Test
    public void test_OneBuyAndSell_Market_Order()
    {
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis())));
        assertEquals(0, OrderProcessor.buyMap.get("HDFC") != null ? OrderProcessor.buyMap.get("HDFC").size() : 0 );
        assertEquals(0, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
    }

    @Test
    public void test_PriceTime_Validation_With2SameBuy_OneSell_Market()
    {
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN,
                OrderType.MARKET,System.currentTimeMillis()));
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(34567L,"HDFC",TradeType.BUY,BigInteger.valueOf(20), BigDecimal.TEN,
                OrderType.MARKET,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(76898L,"HDFC",TradeType.SELL,BigInteger.TEN,
                BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis())));
        assertEquals(1, OrderProcessor.buyMap.get("HDFC").size());
        assertEquals(0, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
        assertEquals(34567L, OrderProcessor.buyMap.get("HDFC").peek().getOrderId());
    }


    @Test
    public void test_PriceTime_Validation_With2SameSELL_OneBuy_Market()
    {
        TestDataCreatorForOrderProcessor.emptyOrders();
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis()));
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(34567L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(76898L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis())));
        assertEquals(0, OrderProcessor.buyMap.get("HDFC") != null ? OrderProcessor.buyMap.get("HDFC").size() : 0);
        assertEquals(1, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
    }

    @Test
    public void test_OneBuy_OneMarketSell_OneLimit_Buy()
    {
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, new BigDecimal("100"),
                OrderType.LIMIT,System.currentTimeMillis()));
        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(34567L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN,
                OrderType.MARKET,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(76898L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN,
                OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(0, OrderProcessor.buyMap.get("HDFC") != null ? OrderProcessor.buyMap.get("HDFC").size() : 0);
        assertEquals(1, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
        assertEquals(BigInteger.TEN, OrderProcessor.sellMap.get("HDFC").peek().getQuantity());
    }

    @Test
    public void test_OneBuy_Higher_Sell()
    {

        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.valueOf(100),
                OrderType.LIMIT,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(76898L,"HDFC",TradeType.SELL,
                BigInteger.valueOf(100), BigDecimal.valueOf(200), OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(1, OrderProcessor.buyMap.get("HDFC") != null ? OrderProcessor.buyMap.get("HDFC").size() : 0);
        assertEquals(1, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );

    }

    @Test
    public void retrieveOrderBookTest() throws ResourceNotFoundException {

        TestDataCreatorForOrderProcessor.prepareOneBuyOrder();
        TestDataCreatorForOrderProcessor.prepareOneSellOrder();

        assertAll(() -> processor.retrieveOrderBook("HDFC"));
        assertEquals(2, processor.retrieveOrderBook("HDFC").size() );
    }

    @Test(expected= ResourceNotFoundException.class)
    public void retrieveOrderBookExpectException() throws ResourceNotFoundException{
        processor.retrieveOrderBook("HDFC");
    }

    @Test
    public void test_Delete() throws ResourceNotFoundException {

        TestDataCreatorForOrderProcessor.prepareOneBuyOrder();
        assertEquals("Successful execution", processor.deleteOrder(1234L).getResponse());
    }

    @Test(expected= ResourceNotFoundException.class)
    public void deleteOrderExpectException() throws ResourceNotFoundException{
        processor.deleteOrder(124L);
    }

    @Test
    public void test_processUpdate_Buy()  {

        TestDataCreatorForOrderProcessor.prepareOneBuyOrder();
        assertAll(() ->  processor.processUpdate(
                TestDataCreatorForOrderProcessor.createTradeOrderEntity(
                        1234L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis())));
    }

    @Test
    public void test_processUpdate_Sell()  {

        TestDataCreatorForOrderProcessor.prepareOneSellOrder();
        assertAll(() ->  processor.processUpdate(
                TestDataCreatorForOrderProcessor.createTradeOrderEntity(
                        1234L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis())));
    }

    @Test(expected= ResourceNotFoundException.class)
    public void test_ProcessUpdate_Expect_Exception() throws ResourceNotFoundException{

        processor.processUpdate(
                TestDataCreatorForOrderProcessor.createTradeOrderEntity(
                        123224L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.TEN, OrderType.MARKET,System.currentTimeMillis()));
    }

    @Test
    public void test_OneBuy_2_Sell()
    {

        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.valueOf(100),
                OrderType.LIMIT,System.currentTimeMillis()));

        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12234L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.valueOf(100),
                OrderType.LIMIT,System.currentTimeMillis()));

        assertAll(() -> processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(56789L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.valueOf(100),
                OrderType.LIMIT,System.currentTimeMillis())));
        assertEquals(1, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );

    }

    @Test
    public void testOrderWithConcurrency() throws InterruptedException {

        processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(12345L,"HDFC",TradeType.BUY,BigInteger.TEN, BigDecimal.valueOf(100),
                OrderType.LIMIT,System.currentTimeMillis()));

        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < 2; i++) {
            service.submit(() -> {
                processor.process(TestDataCreatorForOrderProcessor.createTradeOrderEntity(4567L,"HDFC",TradeType.SELL,BigInteger.TEN, BigDecimal.valueOf(100),
                        OrderType.LIMIT,System.currentTimeMillis()));
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(1, OrderProcessor.sellMap.get("HDFC") != null ? OrderProcessor.sellMap.get("HDFC").size() : 0 );
    }

}
