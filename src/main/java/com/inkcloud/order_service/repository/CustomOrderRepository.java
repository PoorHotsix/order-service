package com.inkcloud.order_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;

public interface CustomOrderRepository {
    abstract Page<Order> searchOrders(OrderSearchCreteria condition, OrderDateCreteria date, OrderSortingCreteria sort, Pageable page);
}
