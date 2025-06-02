package com.inkcloud.order_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;
import com.inkcloud.order_service.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    private String retriveErrMsg = "주문 정보 조회 오류!";
    private String updateErrMsg = "주문 상태 수정 실패!";

    @Override
    public String createOrder(OrderDto dto) {
        Order order = dtoToEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return repo.save(order).getId();
    }

    @Override
    public OrderDto retriveOrder(String orderId) {
        Order order = repo.findById(orderId).orElseThrow(() -> {
            throw new IllegalArgumentException(retriveErrMsg);
        });
        return entityToDto(order);
    }

    @Override
    public List<OrderDto> retriveOrdersByMember(String memberId, LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        return null;
    }

    @Override
    public List<OrderDto> allRetriveOrders(OrderSearchCreteria searchCondition, int page, int size) {
        return null;
    }

    @Override
    public OrderSimpleResponseDto updateOrder(String id) {
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new IllegalArgumentException(updateErrMsg);
        });
        order.setState(order.getState().next());
        return entityToSimpleDto(order);
    }

    @Override
    public OrderSimpleResponseDto cancleOrder(String id) {
        Order order = repo.findById(id).orElseThrow(() -> {
            throw new IllegalArgumentException(updateErrMsg);
        });
        order.cancleOrder();
        return entityToSimpleDto(order);
    }

}
