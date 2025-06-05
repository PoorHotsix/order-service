package com.inkcloud.order_service.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import com.inkcloud.order_service.enums.OrderState;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "order_table")
public class Order {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private int price;
    private int quantity;
    @ColumnDefault("0")  // 또는
    private int shippingFee;


    // ============ 회원(주문자) 정보 ==============
    @Embedded
    private MemberInfo member;
    

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderShip orderShip;

    public void updateOrderState(OrderState state){
        this.state = state.next();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancleOrder(){
        this.state = OrderState.CANCELLD;
        this.updatedAt = LocalDateTime.now();
    }


    // // ============== 편의 메서드 ==============
    // public void addOrderItem(OrderItem orderItem){
    //     orderItems.add(orderItem);
    //     orderItem.setOrder(this);
    // }
    // public void addOrderItem(OrderItem orderItem){
    //     orderItems.add(orderItem);
    //     orderItem.setOrder(this);
    // }
}
