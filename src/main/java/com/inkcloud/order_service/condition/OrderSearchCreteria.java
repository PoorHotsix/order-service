package com.inkcloud.order_service.condition;


import java.util.List;

import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSearchCreteria {
    private OrderSearchCategory keywordCategory;
    private String keyword;
    private List<OrderState> states;
    private List<PaymentMethod> paymentMethods;
}
