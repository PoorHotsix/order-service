package com.inkcloud.order_service.repository;

import org.springframework.data.domain.Page;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;

public interface CustomOrderRepository {
    abstract Page<Order> searchAllOrders(OrderSearchCreteria condition, OrderDateCreteria date, OrderSortingCreteria sort, int size, int page);
    abstract Page<Order> searchMemberOrders(String memberId, OrderDateCreteria date, OrderSortingCreteria sort, int size, int page);
}
