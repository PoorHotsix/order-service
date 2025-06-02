package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
import java.util.List;

import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.dto.MemberInfo;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;

public interface OrderService {
    abstract String createOrder(OrderDto dto);
    abstract OrderDto retriveOrder(String orderId);
    abstract List<OrderDto> retriveOrdersByMember(String memberId, LocalDateTime startDate, LocalDateTime endDate, int page, int size);
    abstract List<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, int page, int size);
    abstract OrderSimpleResponseDto cancleOrder(String id);
    abstract OrderSimpleResponseDto updateOrder(String id);

    default OrderDto entityToDto(Order order){
        return OrderDto.builder()
                            .id(order.getId())
                            .state(order.getState())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
                            .orderItems(order.getOrderItems())
                            .orderShip(order.getOrderShip())
                            .memberEmail(order.getMember().getMemberEmail())
                            .memberContact(order.getMember().getMemberContact())
                            .memberName(order.getMember().getMemberName())
                            .build();
    }
    default Order dtoToEntity(OrderDto dto){
        return Order.builder()
                            .state(dto.getState())
                            .updatedAt(dto.getUpdatedAt())
                            .price(dto.getPrice())
                            .quantity(dto.getQuantity())
                            .orderItems(dto.getOrderItems())
                            .orderShip(dto.getOrderShip())
                            .member(MemberInfo.builder()
                                        .memberEmail(dto.getMemberEmail())
                                        .memberContact(dto.getMemberContact())
                                        .memberName(dto.getMemberName())
                                        .build()
                                        )
                            .build();
    }
    default OrderSimpleResponseDto entityToSimpleDto(Order order){
        return OrderSimpleResponseDto.builder()
                                            .id(order.getId())
                                            .createdAt(order.getCreatedAt())
                                            .updatedAt(order.getUpdatedAt())
                                            .state(order.getState())
                                            .build();
    }
}
