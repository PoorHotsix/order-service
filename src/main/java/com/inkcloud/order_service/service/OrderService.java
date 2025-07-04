package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.MemberInfo;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.domain.OrderItem;
import com.inkcloud.order_service.domain.OrderShip;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderMemberDto;
import com.inkcloud.order_service.dto.OrderReviewDto;
import com.inkcloud.order_service.dto.UpdateOrdersRequestDto;
import com.inkcloud.order_service.dto.child.MemberDto;
import com.inkcloud.order_service.dto.child.OrderItemDto;
import com.inkcloud.order_service.dto.child.OrderShipDto;
import com.inkcloud.order_service.dto.common.OrderSimpleResponseDto;
import com.inkcloud.order_service.dto.event.OrderEventDto;
import com.inkcloud.order_service.dto.event.alert.ToAlertServiceEvent;
import com.inkcloud.order_service.enums.OrderState;

public interface OrderService {
    abstract OrderEventDto createOrder(OrderDto dto, Jwt jwt);
    abstract OrderDto retriveOrder(String orderId, Jwt jwt);
    abstract Page<OrderMemberDto> retriveOrdersByMember(Jwt jwt,String state, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
    abstract Page<OrderReviewDto> retriveOrdersByMemberInShipped(Jwt jwt, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
    abstract Page<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
    abstract OrderDto cancleOrder(String id, Jwt jwt);
    abstract OrderDto updateOrder(String id, OrderState state, Jwt jwt);
    // abstract List<OrderSimpleResponseDto> cancleOrder(List<String> id, Jwt jwt);
    // abstract List<OrderSimpleResponseDto> updateOrder(List<String> id, Jwt jwt);
    abstract List<OrderDto> updateOrder(UpdateOrdersRequestDto dto, Jwt jwt);

    default OrderDto entityToDto(Order order){
        return OrderDto.builder()
                            .id(order.getId())
                            .state(order.getState())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
                            .method(order.getPaymentMethod())
                            .orderItems(order.getOrderItems().stream().map(this::itemEntityToDto).collect(Collectors.toList()))
                            .orderShip(shipEntityToDto(order.getOrderShip()))
                            .member(MemberDto.builder()
                                            .memberEmail(order.getMember().getMemberEmail())
                                            .memberContact(order.getMember().getMemberContact())
                                            .memberName(order.getMember().getMemberName())
                                            .build()
                            )
                            .build();
    }
    default Order dtoToEntity(OrderDto dto){
        return Order.builder()
                            .state(OrderState.PENDING)
                            .updatedAt(dto.getUpdatedAt())
                            .price(dto.getPrice())
                            .quantity(dto.getQuantity())
                            .orderItems(dto.getOrderItems().stream().map(this::itemDtoToEntity).collect(Collectors.toList()))
                            .orderShip(shipDtoToEntity(dto.getOrderShip()))
                            .member(MemberInfo.builder()
                                        .memberEmail(dto.getMember().getMemberEmail())
                                        .memberContact(dto.getMember().getMemberContact())
                                        .memberName(dto.getMember().getMemberName())
                                        .build()
                                        )
                            .createdAt(dto.getCreatedAt() != null? dto.getCreatedAt() : LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
    }

    default ToAlertServiceEvent entityToAlertDto(Order order){
        return ToAlertServiceEvent.builder()
                            .id(order.getId())
                            .state(order.getState())
                            .createdAt(order.getCreatedAt())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
                            .method(order.getPaymentMethod())
                            .orderItems(order.getOrderItems().stream().map(this::itemEntityToDto).collect(Collectors.toList()))
                            .orderShip(shipEntityToDto(order.getOrderShip()))
                            .member(MemberDto.builder()
                                            .memberEmail(order.getMember().getMemberEmail())
                                            .memberContact(order.getMember().getMemberContact())
                                            .memberName(order.getMember().getMemberName())
                                            .build()
                            )
                            .build();
    }

    default OrderMemberDto entityToMemberDto(Order order){
        return OrderMemberDto.builder().id(order.getId())
                                        .state(order.getState())
                                        .createdAt(order.getCreatedAt())
                                        .price(order.getPrice())
                                        .quantity(order.getQuantity())
                                        .orderName(order.getMember().getMemberName())
                                        .receiver(order.getOrderShip().getReceiver())
                                        .delegateProduct(order.getOrderItems().get(0).getName())
                                        .typesNum(order.getOrderItems().size())
                                        .build();
    }

    default OrderReviewDto entityToReviewDto(Order order){
        return OrderReviewDto.builder().orderItems(order.getOrderItems().stream().map(this::itemEntityToDto).collect(Collectors.toList())).build();
    }


    default OrderSimpleResponseDto entityToSimpleDto(Order order){
        return OrderSimpleResponseDto.builder()
                                            .id(order.getId())
                                            .createdAt(order.getCreatedAt())
                                            .updatedAt(order.getUpdatedAt())
                                            .state(order.getState())
                                            .build();
    }
    default OrderItemDto itemEntityToDto(OrderItem item){
        return OrderItemDto.builder()
                                .itemId(item.getItemId())
                                .name(item.getName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .author(item.getAuthor())
                                .publisher(item.getPublisher())
                                .thumbnailUrl(item.getThumbnailUrl())
                                .build();
    }
    default OrderItem itemDtoToEntity(OrderItemDto dto){
        return OrderItem.builder()
                            .name(dto.getName())
                            .price(dto.getPrice())
                            .quantity(dto.getQuantity())
                            .itemId(dto.getItemId())
                            .author(dto.getAuthor())
                            .publisher(dto.getPublisher())
                            .thumbnailUrl(dto.getThumbnailUrl())
                            .build();
    }
    default OrderShipDto shipEntityToDto(OrderShip ship){
        return OrderShipDto.builder()
                                .name(ship.getName())
                                .receiver(ship.getReceiver())
                                .zipcode(ship.getZipcode())
                                .addressMain(ship.getAddressMain())
                                .addressSub(ship.getAddressSub())
                                .contact(ship.getContact())
                                .build();
    }
    default OrderShip shipDtoToEntity(OrderShipDto dto){
        return OrderShip.builder()
                            .name(dto.getName())
                            .receiver(dto.getReceiver())
                            .zipcode(dto.getZipcode())
                            .addressMain(dto.getAddressMain())
                            .addressSub(dto.getAddressSub())
                            .contact(dto.getContact())
                            .build();
    }

    default void setupRelationships(Order order){
        generatedOrderId(order);
        
        order.getOrderItems().forEach(item->item.setOrder(order));
        order.getOrderShip().setOrder(order);
    }

    default void generatedOrderId(Order order){
        String memberHash = String.valueOf(Math.abs(order.getMember().getMemberEmail().hashCode()))
                                            .substring(0, Math.min(8, String.valueOf(Math.abs(order.getMember().getMemberEmail().hashCode())).length()));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0,8);
        order.setId(memberHash + randomSuffix);             
    }
}
