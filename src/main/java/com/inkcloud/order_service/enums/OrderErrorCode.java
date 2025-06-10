package com.inkcloud.order_service.enums;

public enum OrderErrorCode {
    INVALID_ORDER("유효하지 않은 주문"), 
    FAILED_UPDATE_ORDER("주문 상태 변경 실패"),
    FAILED_SUCCCESS_ORDER("최종 완료 상태 변경 실패"),
    INVALID_MEMBER("주문자 정보 불일치");

    private final String defaultMsg;
    OrderErrorCode(String defaultMsg){
        this.defaultMsg=defaultMsg;
    }
    public String getDefaultMsg(){return defaultMsg;}
}
