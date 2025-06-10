package com.inkcloud.order_service.dto;

import java.time.LocalDateTime;

import com.inkcloud.order_service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private PaymentMethod method;
    private int price;
    private int count;

    private LocalDateTime at;
    private String pg;

}
