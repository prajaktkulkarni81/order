package com.trade.stock.order.processor;

import com.trade.stock.order.constants.OrderType;
import com.trade.stock.order.constants.TradeType;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.dao.entity.TradeOrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author prajaktkulkarni
 * This class is brain of stock order processing.
 */
@Component
@Slf4j
public class OrderProcessor {

    public static final String ORDER_DOESN_T_EXISTS_FOR_UPDATION = "Order doesn't exists for updation";
    static Map<String, PriorityBlockingQueue<TradeOrderEntity>> buyMap = new ConcurrentHashMap<>();
    static Map<String, PriorityBlockingQueue<TradeOrderEntity>> sellMap = new ConcurrentHashMap<>();
    private static final int INITIAL_CAPACITY = 100;

    /**
     * This is starting point of order processing.
     * Based on order type flow will differ and will call either to process buy or sell order.
     * @param order
     */
    public void process(TradeOrderEntity order)
    {
        log.info("Process stock order : START");
        TradeType tradeType = order.getTradeType();
        switch (tradeType) {
            case BUY:
                processBuyOrder(order);
                break;

            case SELL:
                processSellOrder(order);
                break;
            default:
                break;
        }

    }


    private void processSellOrder(TradeOrderEntity order) {
        log.info("Process stock SELL order : START");
        //Get all available buy orders.
        PriorityBlockingQueue<TradeOrderEntity> buyOrders = buyMap.get(order.getStockTicker());
        if(buyOrders == null || buyOrders.isEmpty())
        {
            processFirstOrder(order,sellMap);
        }else {
            processIncrementalOrders(order,buyMap);
        }
        log.info("Process stock SELL order : END");
    }

    private void processBuyOrder(TradeOrderEntity order) {
        log.info("Process stock BUY order : START");
        //Get all available sell orders.
        PriorityBlockingQueue<TradeOrderEntity> sellOrders = sellMap.get(order.getStockTicker());
        if(sellOrders == null || sellOrders.isEmpty())
        {
            processFirstOrder(order,buyMap);
        }else {
            processIncrementalOrders(order,sellMap);
        }

        log.info("Process stock BUY order : END");
    }

    private void processFirstOrder(TradeOrderEntity order, Map<String, PriorityBlockingQueue<TradeOrderEntity>> tradeMap) {
        log.info("Process processFirstOrder : START");
        //When no order is present for given stock buy/sell map is added with new order entry.
        PriorityBlockingQueue<TradeOrderEntity> tradeOrderEntities = tradeMap.computeIfAbsent(order.getStockTicker(), s -> new PriorityBlockingQueue<>(INITIAL_CAPACITY, getComparator(order.getTradeType())));
        tradeOrderEntities.put(order);
        log.info("Process processFirstOrder : END");
    }

    private void processIncrementalOrders(TradeOrderEntity order,Map<String, PriorityBlockingQueue<TradeOrderEntity>> tradeMap)
    {
        log.info("ProcessIncrementalOrders : START");
        {
            //Get all orders for given stock ticker
            PriorityBlockingQueue<TradeOrderEntity> tradeOrderEntities = tradeMap.get(order.getStockTicker());
            log.info("processIncrementalOrders tradeOrderEntities for " + order.getStockTicker() +  " are " + tradeOrderEntities.size());
            boolean isFirstOrder = false;
            if(null != tradeOrderEntities && !tradeOrderEntities.isEmpty()) {
                  TradeOrderEntity possibleMatchOrder = tradeOrderEntities.peek();
                        isFirstOrder = processOrder(order,tradeOrderEntities, possibleMatchOrder);
                //Iteratively call processIncrementalOrders till quantity is zero
                if (order.getQuantity().compareTo(BigInteger.ZERO) > 0 && !isFirstOrder) {
                    processIncrementalOrders(order, tradeMap);
                }

            }
        }

        log.info("ProcessIncrementalOrders : END");
    }

    private boolean processMarketOrders(TradeOrderEntity order, PriorityBlockingQueue<TradeOrderEntity> tradeOrderEntities) {
        Collection<TradeOrderEntity> collect = tradeOrderEntities.stream().filter(entity -> entity.getOrderType() == OrderType.MARKET).collect(Collectors.toList());

        if(!collect.isEmpty()) {
            PriorityBlockingQueue<TradeOrderEntity> marketOrders = new PriorityBlockingQueue<>(INITIAL_CAPACITY, getComparator(order.getTradeType()));
            marketOrders.addAll(collect);
            TradeOrderEntity marketMatchOrder = marketOrders.peek();
            return processOrder(order,tradeOrderEntities, marketMatchOrder);
        }else
        {
            if(order.getTradeType() == TradeType.BUY) {
                processFirstOrder(order, buyMap);
            }else
            {
                processFirstOrder(order,sellMap);
            }
            return true;
        }

    }

    /**
     * Important method to process the quantities.
     * Flow is like below :
     * If order quantity > match order quantity
     *   Subtract quantity from match order quantity and set possible match order qty to zero
     *   Remove matched order from Q
     *   Update Map with remaining quantity
     * If order quantity is < match order quantity
     *  Subtract qty from match order qty
     *  Update match order with new qty
     * If order qty and match order qty is equal
     *  Make qty is zero
     *  remove matched order from Q
     * @param order
     * @param tradeOrderEntities
     * @param possibleMatchOrder
     */
    private boolean processOrder(TradeOrderEntity order, PriorityBlockingQueue<TradeOrderEntity> tradeOrderEntities, TradeOrderEntity possibleMatchOrder) {
        boolean hasOrderNoOrderProcess = false;
        log.info("PROCESS ORDER : START");
        if((order.getTradeType() == TradeType.BUY && order.getPrice().compareTo(possibleMatchOrder.getPrice()) >= 0) ||
                (order.getTradeType() == TradeType.SELL && order.getPrice().compareTo(possibleMatchOrder.getPrice()) <= 0)
                || order.getOrderType() == OrderType.MARKET)
        {
            BigInteger originalQty = order.getQuantity();
            BigInteger possibleMatchOrigQty = possibleMatchOrder.getQuantity();
            if (order.getQuantity().compareTo(BigInteger.ZERO) > 0 && order.getQuantity().compareTo(possibleMatchOrder.getQuantity()) > 0) {
                order.setQuantity(order.getQuantity().subtract(possibleMatchOrder.getQuantity()));
                possibleMatchOrder.setQuantity(BigInteger.ZERO);
                boolean removeStatus = tradeOrderEntities.remove(possibleMatchOrder);
                //To check remove is successful . Possible race condition.
                //If 2 orders peek same order then one will be successful . Another order will be retried.
                if(removeStatus)
                {
                    log.info("Possible match executed");
                    if(order.getTradeType() == TradeType.BUY && sellMap.get(order.getStockTicker()).isEmpty())
                    {
                        processFirstOrder(order,buyMap);
                    }else if(order.getTradeType() == TradeType.SELL && buyMap.get(order.getStockTicker()).isEmpty())
                    {
                        processFirstOrder(order,sellMap);
                    }
                }else
                {
                    log.info("Trade Removal Unsuccessful ***");
                    possibleMatchOrder.setQuantity(possibleMatchOrigQty);
                    order.setQuantity(originalQty);
                    log.info("Retry Order with QTY " + originalQty);
                    process(order);
                }

            } else if (order.getQuantity().compareTo(BigInteger.ZERO) > 0 && order.getQuantity().compareTo(possibleMatchOrder.getQuantity()) < 0) {
                possibleMatchOrder.setQuantity(possibleMatchOrder.getQuantity().subtract(order.getQuantity()));
                order.setQuantity(BigInteger.ZERO);
            } else {
                order.setQuantity(BigInteger.ZERO);
                possibleMatchOrder.setQuantity(BigInteger.ZERO);
                boolean removeStatus = tradeOrderEntities.remove(possibleMatchOrder);
                //To check remove is successful . Possible race condition.
                //If 2 orders peek same order then one will be successful . Another order will be retried.
                if(!removeStatus)
                {
                    log.info("Trade Removal Unsuccessful");
                    possibleMatchOrder.setQuantity(possibleMatchOrigQty);
                    order.setQuantity(originalQty);
                    log.info("Retry Order with QTY " + originalQty);
                    process(order);
                }else
                {
                    log.info("Possible match executed**");
                }

            }


        }else
        {
            if(order.getTradeType() == TradeType.BUY )
            {
                processFirstOrder(order,buyMap);
            }else if(order.getTradeType() == TradeType.SELL)
            {
                processFirstOrder(order,sellMap);
            }
            hasOrderNoOrderProcess = true;
        }
        log.info("PROCESS ORDER : END");
       return hasOrderNoOrderProcess;
    }

    /**
     * Important function for order processing.
     * Returns comparator for BUY and SELL orders to be stored in priorityBlockingQueue
     * priorityBlockingQueue is stored orders with following order
     *   BUY :
     *      ORDER TYPE ( MARKET/LIMIT )
     *      Quantity ( Highest to Lowest )
     *      Time ( Order creation time )
     *   SELL :
     *      ORDER TYPE ( MARKET/LIMIT )
     *      Quantity ( Lowest to Highest )
     *      Time ( Order creation time )
     * @param tradeType
     * @return
     */
    private Comparator<TradeOrderEntity> getComparator(TradeType tradeType) {
            return tradeType == TradeType.BUY ?
                    Comparator.comparing(TradeOrderEntity::getOrderType)
                            .thenComparing(TradeOrderEntity::getPrice)
                            .thenComparing(TradeOrderEntity::getTradeTime) :  Comparator
                    .comparing(TradeOrderEntity::getOrderType)
                    .thenComparing((o1, o2) -> Double.compare(o2.getPrice().doubleValue() , o1.getPrice().doubleValue()))
                    .thenComparing(TradeOrderEntity::getTradeTime);

        }

    /**
     * Get all the available orders for given stock.
     * @param stockName
     * @return
     * @throws ResourceNotFoundException
     */
    public List<TradeOrderEntity> retrieveOrderBook(String stockName) throws ResourceNotFoundException {
        log.info("retrieveOrderBook : START");
        List<TradeOrderEntity> orderBook = new ArrayList<>();

        PriorityBlockingQueue<TradeOrderEntity> buyList = buyMap.get(stockName);
        PriorityBlockingQueue<TradeOrderEntity> sellList = sellMap.get(stockName);
        if(null != buyList && !buyList.isEmpty())
        {
            orderBook.addAll(buyList);
        }

        if(null != sellList && !sellList.isEmpty()) {
            orderBook.addAll(sellList);
        }

        if(orderBook.isEmpty())
        {
            log.error("No Order book exist");
            throw new ResourceNotFoundException("No Order book exist for " + stockName);
        }
        log.info("retrieveOrderBook : END");
        return orderBook;
    }

    /**
     * Delete order for given order id.
     * @param orderId
     * @return
     * @throws ResourceNotFoundException
     */
    public OrderResponse deleteOrder(Long orderId) throws ResourceNotFoundException {
        log.info("deleteOrder : START");
        OrderResponse response = new OrderResponse();

        boolean orderIdRemoved = false;
        //Delete order from map if order id matches
        for (Map.Entry<String, PriorityBlockingQueue<TradeOrderEntity>> entry : buyMap.entrySet()) {
            orderIdRemoved = entry.getValue().removeIf(b -> b.getOrderId().compareTo(orderId) == 0);
        }

        for (Map.Entry<String, PriorityBlockingQueue<TradeOrderEntity>> entry : sellMap.entrySet()) {
            orderIdRemoved = entry.getValue().removeIf(b -> b.getOrderId().compareTo(orderId)==0);
        }
        if(orderIdRemoved) {
            response.setResponse("Successful execution");
        }else
        {
            log.error("OrderId doesn't exist for removal");
            throw new ResourceNotFoundException("OrderId doesn't exist for removal");
        }
        log.info("deleteOrder : END");
        return response;
    }

    /**
     * Update Order with new order.
     * @param tradeOrderEntity
     * @throws ResourceNotFoundException
     */
    public void processUpdate(TradeOrderEntity tradeOrderEntity) throws ResourceNotFoundException {

        log.info("processUpdate : START");
        TradeType tradeType = tradeOrderEntity.getTradeType();
        switch (tradeType) {
            case BUY:
                PriorityBlockingQueue<TradeOrderEntity> tradeBuyOrderEntities = buyMap.get(tradeOrderEntity.getStockTicker());
                processUpdate(tradeOrderEntity, tradeBuyOrderEntities);
                break;

            case SELL:
                PriorityBlockingQueue<TradeOrderEntity> tradeSellOrderEntities = sellMap.get(tradeOrderEntity.getStockTicker());
                processUpdate(tradeOrderEntity, tradeSellOrderEntities);
                break;
            default:
                break;
        }

    }

    private void processUpdate(TradeOrderEntity tradeOrderEntity, PriorityBlockingQueue<TradeOrderEntity> tradeOrderEntities) throws ResourceNotFoundException {
        log.info("processUpdate : START");
        //Update Order if order id matches
        if (null != tradeOrderEntities && !tradeOrderEntities.isEmpty()) {
            Optional<TradeOrderEntity> matchedOrder = tradeOrderEntities.stream().filter(b -> b.getOrderId().compareTo(tradeOrderEntity.getOrderId()) == 0).findFirst();
            if (matchedOrder.isPresent()) {
                tradeOrderEntities.remove(matchedOrder.get());
                tradeOrderEntities.add(tradeOrderEntity);
            }else{
                log.error(ORDER_DOESN_T_EXISTS_FOR_UPDATION);
                throw new ResourceNotFoundException(ORDER_DOESN_T_EXISTS_FOR_UPDATION);
            }
        }else{
            log.error(ORDER_DOESN_T_EXISTS_FOR_UPDATION);
            throw new ResourceNotFoundException(ORDER_DOESN_T_EXISTS_FOR_UPDATION);
        }

    }
}
