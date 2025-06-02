package com.inkcloud.order_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "order_item")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private long itemId;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String auther;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
