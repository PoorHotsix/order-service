package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;
import com.inkcloud.order_service.dto.OrderStartEvent;
import com.inkcloud.order_service.dto.OrderStartEventDto;
import com.inkcloud.order_service.dto.OrderSuccessEvent;
import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    private final KafkaTemplate<String, OrderStartEvent> kafkaTemplate; // 토픽 이름, event객체

    private String retriveErrMsg = "주문 정보 조회 오류!";
    private String updateErrMsg = "주문 상태 수정 실패!";

    @Override
    public OrderStartEventDto createOrder(OrderDto dto) {
        Order order = dtoToEntity(dto);
        setupRelationships(order);

        OrderStartEvent event = new OrderStartEvent();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = order.getId() + timestamp;
        String hash = DigestUtils.sha256Hex(combined).substring(0, 16);

        String paymentId = "PAY_" + hash.toUpperCase();
        OrderStartEventDto resDto = OrderStartEventDto.builder()
                .email(dto.getMember().getMemberEmail())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .paymentId(paymentId)
                .orderId(order.getId())
                .build();
        event.setOrder(resDto);
        event.setId(order.getId());


        Map<String, String> res = new HashMap<>();
        res.put("orderId", order.getId());
        res.put("paymentId", paymentId);

        kafkaTemplate.send("order-verify", event);
        repo.save(order);

        return resDto;
    }

    @Override
    public OrderDto retriveOrder(String orderId) {
        Order order = repo.findById(orderId).orElseThrow(() -> {
            throw new IllegalArgumentException(retriveErrMsg);
        });
        return entityToDto(order);
    }

    @Override
    public Page<OrderDto> retriveOrdersByMember(String memberId, OrderDateCreteria date, OrderSortingCreteria sort,
            Pageable page) {
        Page<Order> orders = repo.searchOrders(OrderSearchCreteria.builder()
                .keywordCategory(OrderSearchCategory.MEMBER_EMAIL)
                .keyword(memberId)
                .build(), date, sort, page);
        return orders.map(this::entityToDto);
    }

    @Override
    public Page<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, OrderDateCreteria date,
            OrderSortingCreteria sort, Pageable page) {
        Page<Order> orders = repo.searchOrders(searchCondition, date, sort, page);

        return orders.map(this::entityToDto);
    }

    @Override
    public OrderSimpleResponseDto updateOrder(String id) {
        log.info("============주문 업데이트 : {} ", id);
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new IllegalArgumentException(updateErrMsg);
        });
        log.info("============주문 조회 : {} ", order.getId());
        order.setState(order.getState().next());
        order.setUpdatedAt(LocalDateTime.now());
        return entityToSimpleDto(order);
    }

    @Override
    public OrderSimpleResponseDto cancleOrder(String id) {
        log.info("============주문 업데이트 : {} ", id);
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new IllegalArgumentException(updateErrMsg);
        });
        log.info("============주문 조회 : {} ", order.getId());
        order.setUpdatedAt(LocalDateTime.now());
        order.cancleOrder();
        return entityToSimpleDto(order);
    }

    @KafkaListener(topics = "payment-success", groupId = "payment_group")
    @Transactional
    public void orderComplete(String event) throws Exception {
        log.info("Kafka Consumer : Order-Service, receive event : {}", event);

        try {
            OrderSuccessEvent successEvent = new ObjectMapper().readValue(event, OrderSuccessEvent.class);
            Optional<Order> result = repo.findById(successEvent.getOrderId());
            Order order = result.orElseThrow(() -> {
                throw new RuntimeException("주문번호 : " + successEvent.getOrderId() + "에 해당하는 주문이 없음");
            });
            order.setState(order.getState().next());
            log.info("주문 완료");

            // 여기 주문 완료 이벤트 발행
        } catch (Exception e) {
            throw e;
        }
    }

}
