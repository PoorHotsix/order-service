package com.inkcloud.order_service.condition;


import java.util.List;

import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.enums.PaymentMethod;

public class OrderSearchCreteria {
    private OrderSearchCategory keywordCategory;
    private String keyword;
    private List<OrderState> states;
    private List<PaymentMethod> paymentMethods;
}
