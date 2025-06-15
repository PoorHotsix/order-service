package com.inkcloud.order_service.dto.event.alert;

import java.time.LocalDateTime;
import java.util.List;

import com.inkcloud.order_service.dto.child.MemberDto;
import com.inkcloud.order_service.dto.child.OrderItemDto;
import com.inkcloud.order_service.dto.child.OrderShipDto;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ToAlertServiceEvent {
    private String id; // ㅇ
    private OrderState state; // ㅇ
    private LocalDateTime createdAt; // ㅇ
    
    private int price; // ㅇ
    private int quantity; // ㅇ 
    private PaymentMethod method;
    
    private MemberDto member; // ㅇ 

    private List<OrderItemDto> orderItems; // ㅇ
    private OrderShipDto orderShip; // ㅇ
}
