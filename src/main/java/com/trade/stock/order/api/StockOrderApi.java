package com.trade.stock.order.api;

import com.trade.stock.order.dao.entity.TradeOrderEntity;
import com.trade.stock.order.exception.ResourceNotFoundException;
import com.trade.stock.order.model.OrderResponse;
import com.trade.stock.order.model.TradeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.trade.stock.order.constants.Constants.*;

/**
 * @author  Prajakt Kulkarni.
 * This interface provide contract to maintain stock order book.
 */
public interface StockOrderApi {

    /**
     *
     * @param tradeRequest
     * @return OrderResponse
     * This method provides functionality to create trade order for given stock.
     */
    @Operation(summary = "Generate Stock trade order for given stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generate Stock trade order for given stock",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeRequest.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Trade Type",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Missing stock ticker",
                    content = @Content) })
    @PostMapping(value = ORDER, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderResponse> stockOrderCreate(@Valid @RequestBody TradeRequest tradeRequest);

    /**
     *
     * @param orderId
     * @param tradeRequest
     * @return orderResponse
     * @throws ResourceNotFoundException
     *
     * This method provides functionality to update existing stock order. Throws ResourceNotFoundException if given order
     * doesn't exists.
     */
    @Operation(summary = "Update Stock trade order for given order id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update Stock trade order for given order id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TradeRequest.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Trade Type",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Missing stock ticker",
                    content = @Content) })
    @PutMapping(value = ORDER+ORDER_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderResponse> stockOrderCreateUpdate(@PathVariable("orderId") Long orderId ,@Valid @RequestBody TradeRequest tradeRequest) throws ResourceNotFoundException ;

    /**
     *
     * @param stockTicker
     * @return List<TradeOrderEntity>
     * @throws ResourceNotFoundException
     * This method provide functionality to retrieve entire order book for given stock.
     * Throws ResourceNotFoundException if given stock doesn't have any orders.
     */
    @Operation(summary = "Retrieve order book for given stock ticker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieve order book for given stock ticker",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "404", description = "No data found",
                    content = @Content) })
    @GetMapping(value = ORDER+STOCK_NAME, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<TradeOrderEntity>> retrieveOrderBook(@NotNull @PathVariable("stockTicker") String stockTicker) throws ResourceNotFoundException;

    /**
     *
     * @param orderId
     * @return OrderResponse
     * @throws ResourceNotFoundException
     * This method provide functionality to delete any existing order from order book.Throws ResourceNotFoundException if given order
     * doesn't exists.
     */
    @Operation(summary = "Delete order for given orderId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete order for given orderId",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class)) }),
            @ApiResponse(responseCode = "404", description = "No data found",
                    content = @Content) })
    @DeleteMapping(value = ORDER+ORDER_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderResponse> deleteOrder(@NotNull @PathVariable("orderId") Long orderId) throws ResourceNotFoundException ;
}
