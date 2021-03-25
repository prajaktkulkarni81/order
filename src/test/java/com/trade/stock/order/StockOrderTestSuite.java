package com.trade.stock.order;

import com.trade.stock.order.controller.StockOrderControllerTest;
import com.trade.stock.order.integration.StockOrderIntegrationTest;
import com.trade.stock.order.processor.OrderProcessorTest;
import com.trade.stock.order.service.OrderBookServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({StockOrderIntegrationTest.class,StockOrderControllerTest.class, OrderBookServiceImplTest.class, OrderProcessorTest.class})
public class StockOrderTestSuite {
}
