package com.inkcloud.order_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.domain.QOrder;
import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.enums.OrderState;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {
    private final JPAQueryFactory qFactory;
    private final QOrder qOrder;

    @Override
    public Page<Order> searchAllOrders(OrderSearchCreteria condition, OrderDateCreteria date, OrderSortingCreteria sort,
            int size, int page) {

        return null;
    }

    @Override
    public Page<Order> searchMemberOrders(String memberId, OrderDateCreteria date, OrderSortingCreteria sort, int size,
            int page) {
        return null;
    }

    private BooleanExpression searchCategoryEQ(OrderSearchCategory category, String keyword) {
        if(keyword == null || keyword == "" || category == null)
            return null;

        switch (category) {
            case ID:
                return qOrder.id.eq(keyword);
            case MEMBER_EMAIL:
                return qOrder.member.memberEmail.eq(keyword);
            case MEMBER_NAME:
                return qOrder.member.memberName.eq(keyword);
            case RECEIVER:
                return qOrder.orderShip.receiver.eq(keyword);
            default:
                return null;
        }
    }

    private BooleanExpression searchDateEQ(OrderDateCreteria date){
        if(date == null || date.getStartDate() == null || date.getEndDate() == null) return null;

        return qOrder.createdAt.between(date.getStartDate(), date.getEndDate());
    }

    private BooleanExpression searchStateEQ(List<OrderState> states){
        if(states == null || states.isEmpty()) return null;
        return qOrder.state.in(states);
    }
}
