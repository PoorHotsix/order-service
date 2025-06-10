package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
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
import com.inkcloud.order_service.dto.MemberDto;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderEventDto;
import com.inkcloud.order_service.dto.OrderItemDto;
import com.inkcloud.order_service.dto.OrderShipDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;
import com.inkcloud.order_service.enums.OrderState;

public interface OrderService {
    abstract OrderEventDto createOrder(OrderDto dto, Jwt jwt);
    abstract OrderDto retriveOrder(String orderId);
    abstract Page<OrderDto> retriveOrdersByMember(String memberId, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
    abstract Page<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
    abstract OrderSimpleResponseDto cancleOrder(String id, Jwt jwt);
    abstract OrderSimpleResponseDto updateOrder(String id, Jwt jwt);

    default OrderDto entityToDto(Order order){
        return OrderDto.builder()
                            .id(order.getId())
                            .state(order.getState())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .price(order.getPrice())
                            .quantity(order.getQuantity())
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
