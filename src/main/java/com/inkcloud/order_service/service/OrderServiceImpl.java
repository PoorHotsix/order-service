package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.dto.MemberDto;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderEvent;
import com.inkcloud.order_service.dto.OrderEventDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;
import com.inkcloud.order_service.enums.OrderErrorCode;
import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.exception.OrderException;
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
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate; // 토픽 이름, event객체
    private final WebClient webClient;

    private String retriveErrMsg = "주문 정보 조회 오류!";
    private String updateErrMsg = "주문 상태 수정 실패!";

    @Override
    public OrderEventDto createOrder(OrderDto dto, Jwt jwt) {
        log.info("token : {}",jwt.getTokenValue());
        JsonNode res = webClient.get()
                                .uri("/api/v1/members/detail")
                                .header("Authorization", "Bearer "+jwt.getTokenValue())
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .block();
        log.info("Get User Detail : {}", res);
        MemberDto mem = MemberDto.builder()
                                    .memberEmail(res.get("email").asText())
                                    .memberContact(res.get("phoneNumber").asText())
                                    .memberName(res.get("firstName").asText()+res.get("lastName").asText())
                                    .build();
        dto.setMember(mem);
        log.info("user info success : {}", res.get("email").asText());

        Order order = dtoToEntity(dto);
        setupRelationships(order);

        OrderEvent event = new OrderEvent();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = order.getId() + timestamp;
        String hash = DigestUtils.sha256Hex(combined).substring(0, 16);

        String paymentId = "PAY_" + hash.toUpperCase();
        OrderEventDto resDto = OrderEventDto.builder()
                .email(dto.getMember().getMemberEmail())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .paymentId(paymentId)
                .orderId(order.getId())
                .build();
        event.setOrder(resDto);
        event.setId(order.getId());

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
            throw new OrderException(OrderErrorCode.FAILED_UPDATE_ORDER);
        });
        log.info("============주문 조회 : {} ", order.getId());
        order.setState(order.getState().next());
        return entityToSimpleDto(order);
    }

    @Override
    public OrderSimpleResponseDto cancleOrder(String id) {
        log.info("============주문 취소 : {} ", id);
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new OrderException(OrderErrorCode.FAILED_UPDATE_ORDER);
        });
        log.info("============주문 조회 : {} ", order.getId());
        order.cancleOrder();
        return entityToSimpleDto(order);
    }

    // 결제 완료시점
    @KafkaListener(topics = "payment-success", groupId = "payment_group")
    @Transactional
    public void orderComplete(String event) throws Exception {
        log.info("Kafka Consumer : Order-Service, receive event : {}", event);
        OrderEvent ev = new ObjectMapper().readValue(event, OrderEvent.class);

        try {
            Optional<Order> result = repo.findById(ev.getOrder().getOrderId());
            Order order = result.orElseThrow(() -> {
                throw new OrderException(OrderErrorCode.FAILED_SUCCCESS_ORDER,"주문번호 : " + ev.getOrder().getOrderId() + "에 해당하는 주문이 없음");
            });
            order.setState(order.getState().next());
            log.info("주문 완료");

        } catch (Exception e) {
            log.error("주문 실패", e);
            // 주문 완료 처리 오류시
            kafkaTemplate.send("order-failed", ev);
            throw e;
        }
    }

    // 주문 실패 처리 마무리
    @KafkaListener(topics = "order-failed-payment-refund", groupId = "payment_group")
    @Transactional
    public void failedPay(String event) throws Exception{
        log.info("kafka Consuner : Order-service, receive event: {}", event);
        OrderEvent ev = new ObjectMapper().readValue(event,OrderEvent.class);
        try {
            Optional<Order> result = repo.findById(ev.getOrder().getOrderId());
            Order order = result.orElseThrow(() -> {
                throw new OrderException(OrderErrorCode.FAILED_SUCCCESS_ORDER,"주문번호 : " + ev.getOrder().getOrderId() + "에 해당하는 주문이 없음");
            });
            order.setState(OrderState.FAILED);
        } catch (Exception e) {
            log.error("주문 실패 처리중 오류 : ", e);
            throw e;
        }
    }

}
