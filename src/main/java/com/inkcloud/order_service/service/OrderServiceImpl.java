package com.inkcloud.order_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderMemberDto;
import com.inkcloud.order_service.dto.OrderReviewDto;
import com.inkcloud.order_service.dto.child.MemberDto;
import com.inkcloud.order_service.dto.child.OrderItemDto;
import com.inkcloud.order_service.dto.child.PaymentDto;
import com.inkcloud.order_service.dto.common.OrderSimpleResponseDto;
import com.inkcloud.order_service.dto.event.OrderEvent;
import com.inkcloud.order_service.dto.event.OrderEventDto;
import com.inkcloud.order_service.dto.event.bestseller.ToBestSellerEvent;
import com.inkcloud.order_service.dto.event.product.ToProductEvent;
import com.inkcloud.order_service.dto.event.product.ToProductEventDto;
import com.inkcloud.order_service.dto.event.stat.ToStatEvent;
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
    private final KafkaTemplate<String, Object> kafkaTemplate; // 토픽 이름, event객체
    private final WebClient webClient;

    private String retriveErrMsg = "주문 정보 조회 오류!";
    private String updateErrMsg = "주문 상태 수정 실패!";

    @Override
    public OrderEventDto createOrder(OrderDto dto, Jwt jwt) {
        log.info("token : {}", jwt.getTokenValue());
        JsonNode res = webClient.get()
                .uri("/api/v1/members/detail")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        log.info("Get User Detail : {}", res);
        MemberDto mem = MemberDto.builder()
                .memberEmail(res.get("email").asText())
                .memberContact(res.get("phoneNumber").asText())
                .memberName(res.get("lastName").asText() +res.get("firstName").asText())
                .build();

        if (!dto.getMember().equals(mem)) {
            log.info("dto : {}", dto.getMember());
            log.info("memser : {}", mem);
            throw new OrderException(OrderErrorCode.INVALID_MEMBER);
        }

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
        dto.getOrderItems().forEach(item -> {
            log.info("item : {}", item);
        });

        kafkaTemplate.send("order-verify", event);
        repo.save(order);

        return resDto;
    }

    @Override
    public OrderDto retriveOrder(String orderId, Jwt jwt) {
        Order order = repo.findById(orderId).orElseThrow(() -> {
            throw new IllegalArgumentException(retriveErrMsg);
        });
        PaymentDto dto = webClient.get()
                                    .uri("/api/v1/payments?order_id="+order.getId())
                                    .header("Authorization", "Bearer " + jwt.getTokenValue())
                                    .retrieve()
                                    .bodyToMono(PaymentDto.class)
                                    .block();
        OrderDto od = entityToDto(order);
        od.setPaymentDto(dto);
        return od;
    }

    @Override
    public Page<OrderMemberDto> retriveOrdersByMember(Jwt jwt,String state, OrderDateCreteria date, OrderSortingCreteria sort,
            Pageable page) {
        OrderState oState = null;
        if(!"ALL".equals(state))
            oState = OrderState.valueOf(state);
        List<OrderState> states = oState != null ? List.of(oState) : List.of(OrderState.PREPARE, OrderState.SHIPPING, OrderState.SHIPPED, OrderState.CANCELED);
        
        Page<Order> orders = repo.searchOrders(OrderSearchCreteria.builder()
                .keywordCategory(OrderSearchCategory.MEMBER_EMAIL)
                .keyword(jwt.getClaimAsString("email"))
                .states(states)
                .build(), date, sort, page);
        return orders.map(this::entityToMemberDto);
    }

    @Override
    public Page<OrderReviewDto> retriveOrdersByMemberInShipped(Jwt jwt, OrderDateCreteria date, OrderSortingCreteria sort,
            Pageable page) {
        Page<Order> orders = repo.searchOrders(OrderSearchCreteria.builder()
                .keywordCategory(OrderSearchCategory.MEMBER_EMAIL)
                .keyword(jwt.getClaimAsString("email"))
                .states(List.of(OrderState.SHIPPED))
                .build(), date, sort, page);
        return orders.map(this::entityToReviewDto);
    }


    @Override
    public Page<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, OrderDateCreteria date,
            OrderSortingCreteria sort, Pageable page) {
        Page<Order> orders = repo.searchOrders(searchCondition, date, sort, page);

        return orders.map(this::entityToDto);
    }

    @Override
    public OrderSimpleResponseDto updateOrder(String id, Jwt jwt) {
        log.info("============주문 업데이트 : {} ", id);
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new OrderException(OrderErrorCode.FAILED_UPDATE_ORDER);
        });
        log.info("============주문 조회 : {} ", order.getId());
        order.setState(order.getState().next());
        return entityToSimpleDto(order);
    }

    @Override
    public OrderSimpleResponseDto cancleOrder(String id, Jwt jwt) {
        log.info("============주문 취소 : {} ", id);
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new OrderException(OrderErrorCode.FAILED_UPDATE_ORDER);
        });
        log.info("============주문 조회 : {} ", order.getId());

        Map<String, String> req = new HashMap<>();
        req.put("order_id", id);
        String result = webClient.put()
                .uri("/api/v1/payments")
                .bodyValue(req)
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("=========== 전체 JSON 응답 성공하긴 하냐");
        log.info("=========== 전체 JSON 응답 : {}", result);
        if (result.contains("CANCELLED")) {
            log.info("=========== 결제 취소 완료 : {}", result);
            order.cancleOrder();
        }
        return entityToSimpleDto(order);
    }

    // 결제 완료시점
    @KafkaListener(topics = "payment-success", groupId = "payment_group")
    @Transactional
    public void paymentComplete(String event) throws Exception {
        log.info("Kafka Consumer : Order-Service, receive event : {}", event);
        OrderEvent ev = new ObjectMapper().readValue(event, OrderEvent.class);

        try {
            Optional<Order> result = repo.findById(ev.getOrder().getOrderId());
            Order order = result.orElseThrow(() -> {
                throw new OrderException(OrderErrorCode.FAILED_SUCCCESS_ORDER,
                        "주문번호 : " + ev.getOrder().getOrderId() + "에 해당하는 주문이 없음");
            });
            
            log.info("결제 성공, 상품 증감 이벤트 발행");
            List<OrderItemDto> items = order.getOrderItems().stream()
                                                                .map(this::itemEntityToDto)
                                                                .collect(Collectors.toList());
            List<ToProductEventDto> dtos = new ArrayList<>();
            items.forEach(item->{
                dtos.add(new ToProductEventDto(item.getItemId(), item.getQuantity()));
            }); 
            if(dtos.isEmpty())
                throw new OrderException(OrderErrorCode.INVALID_ITEM);
            kafkaTemplate.send("stock-change", ToProductEvent.builder().orderId(order.getId()).dtos(dtos).build());
            

        } catch (Exception e) {
            log.error("주문 실패", e);
            // 주문 완료 처리 오류시
            kafkaTemplate.send("order-failed", ev);
            throw e;
        }
    }


    @KafkaListener(topics = "stock-confirm", groupId = "payment_group")
    @Transactional
    public void orderComplete(String event) throws Exception {
        log.info("Kafka Consumer : Order-Service, receive event : {}", event);
        OrderEvent ev = new ObjectMapper().readValue(event, OrderEvent.class);

        try {
            Optional<Order> result = repo.findById(ev.getOrder().getOrderId());
            Order order = result.orElseThrow(() -> {
                throw new OrderException(OrderErrorCode.FAILED_SUCCCESS_ORDER,
                        "주문번호 : " + ev.getOrder().getOrderId() + "에 해당하는 주문이 없음");
            });
            order.setState(order.getState().next());
            log.info("주문 완료");

            ToStatEvent statEvent = new ToStatEvent(order.getId(), order.getQuantity(),order.getPrice(),order.getCreatedAt());
            ToBestSellerEvent bestSellerEvent = new ToBestSellerEvent(order.getOrderItems().stream().map(this::itemEntityToDto).collect(Collectors.toList()));
            kafkaTemplate.send("order-complete-bestseller", bestSellerEvent);
            kafkaTemplate.send("order-complete-stat", statEvent);
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
    public void failedPay(String event) throws Exception {
        log.info("kafka Consuner : Order-service, receive event: {}", event);
        String ev = new ObjectMapper().readValue(event, String.class);
        try {
            Optional<Order> result = repo.findById(ev);
            Order order = result.orElseThrow(() -> {
                throw new OrderException(OrderErrorCode.FAILED_SUCCCESS_ORDER,
                        "주문번호 : " + ev + "에 해당하는 주문이 없음");
            });
            order.setState(OrderState.FAILED);
        } catch (Exception e) {
            log.error("주문 실패 처리중 오류 : ", e);
            throw e;
        }
    }

}
