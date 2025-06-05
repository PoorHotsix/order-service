package com.inkcloud.order_service.exception;

import com.inkcloud.order_service.enums.OrderErrorCode;

public class OrderException extends RuntimeException {
    private final OrderErrorCode code;

    public OrderException(OrderErrorCode errorCode){
        super(errorCode.getDefaultMsg());
        this.code=errorCode;
    }
    public OrderException(OrderErrorCode errorCode, String customMessage){
        super(customMessage);
        this.code =errorCode;
    }
    public OrderErrorCode getErrorCode(){
        return code;
    }
}
