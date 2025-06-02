package com.inkcloud.order_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.inkcloud.order_service.domain.Order;

public interface OrderRepository extends JpaRepository<Order, String>, CustomOrderRepository {
    // @Modifying
    // @Query("UPDATE Order o SET o.state = :state, o.updatedAt = :updatedAt WHERE o.id = :id")
    // void updateOrder(@Param("id") String id, @Param("state") OrderState state,
    //         @Param("updatedAt") LocalDateTime updatedAt);
}
