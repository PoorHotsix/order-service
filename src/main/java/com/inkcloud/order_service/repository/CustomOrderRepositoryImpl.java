package com.inkcloud.order_service.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.domain.QOrder;
import com.inkcloud.order_service.domain.QOrderItem;
import com.inkcloud.order_service.domain.QOrderShip;
import com.inkcloud.order_service.enums.OrderSearchCategory;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.enums.SortDirection;
import com.inkcloud.order_service.enums.SortField;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CustomOrderRepositoryImpl implements CustomOrderRepository {
    private final JPAQueryFactory qFactory;
    private final QOrder qOrder = QOrder.order;
    private final QOrderItem qOrderItem = QOrderItem.orderItem;
    private final QOrderShip qOrderShip = QOrderShip.orderShip;

    @Override
    public Page<Order> searchOrders(OrderSearchCreteria condition, OrderDateCreteria date, OrderSortingCreteria sort,
            Pageable page) {
        BooleanExpression categoryFilter = searchCategoryEQ(condition.getKeywordCategory(), condition.getKeyword());
        BooleanExpression dateFilter = searchDateEQ(date);
        BooleanExpression stateFilter = searchStateEQ(condition.getStates());

        List<Order> orders = qFactory.selectFrom(qOrder)
                                    .where(categoryFilter, dateFilter, stateFilter)
                                    .orderBy(getOrderSpecifier(sort))
                                    .offset(page.getOffset())
                                    .limit(page.getPageSize())
                                    .fetch();

        if(!orders.isEmpty()){
            List<String> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            qFactory.selectFrom(qOrderItem)
                    .where(qOrderItem.order.id.in(orderIds))
                    .fetch();
            qFactory.selectFrom(qOrderShip)
                    .where(qOrderShip.order.id.in(orderIds))
                    .fetch();
        }
        Long total = qFactory.select(qOrder.count())
                                .from(qOrder)
                                .where(categoryFilter, dateFilter,stateFilter)
                                .fetchOne();
        return new PageImpl<>(orders, page, total != null? total : 0);
    }

    private BooleanExpression searchCategoryEQ(OrderSearchCategory category, String keyword) {
        if (keyword == null || keyword.isEmpty() || category == null)
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

    private BooleanExpression searchDateEQ(OrderDateCreteria date) {
        if (date == null || date.getStartDate() == null || date.getEndDate() == null)
            return null;

        return qOrder.createdAt.between(date.getStartDate().atStartOfDay(), date.getEndDate().plusDays(1).atStartOfDay());
    }

    private BooleanExpression searchStateEQ(List<OrderState> states) {
        if (states == null || states.isEmpty())
            return null;
        
        List<OrderState> filteredStates = states.stream().filter(state-> state!=OrderState.PENDING).collect(Collectors.toList());

        return qOrder.state.in(filteredStates);
    }

    private OrderSpecifier<?> getOrderSpecifier(OrderSortingCreteria sort) {
        if (sort == null || sort.getSortBy() == null || sort.getSortDir() == null) {
            return qOrder.createdAt.desc();
        }
        ComparableExpressionBase<?> field = getFieldExpression(sort.getSortBy());

        return sort.getSortDir() == SortDirection.DESC ? field.desc() : field.asc();
    }

    private ComparableExpressionBase<?> getFieldExpression(SortField sortField) {
        switch (sortField) {
            case CREATED_AT:
                return qOrder.createdAt;
            case UPDATED_AT:
                return qOrder.updatedAt;
            case PRICE:
                return qOrder.price;
            case STATE:
                return qOrder.state;
            case EMAIL:
                return qOrder.member.memberEmail;
            case NAME:
                return qOrder.member.memberName;
            default:
                return qOrder.createdAt;
        }
    }
}
