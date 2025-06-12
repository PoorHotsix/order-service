package com.inkcloud.order_service.enums;

public enum OrderState {
    PENDING, PREPARE, SHIPPING, SHIPPED, CANCELED, FAILED;

    public OrderState next(){
        OrderState state[] = OrderState.values();
        int ordinal = this.ordinal();
        return ordinal+1 < state.length && !(state[ordinal+1] == CANCELED || state[ordinal+1] == FAILED)? state[ordinal +1] : this; 
    }

}
