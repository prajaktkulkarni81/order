package com.trade.stock.order.exception;

public class ResourceNotFoundException extends Exception{

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
