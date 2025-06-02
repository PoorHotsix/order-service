package com.inkcloud.order_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "order_ship")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderShip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ship_id")
    private long id;

    private String name;

    private String receiver;
    private int zipcode;

    @Column(name = "address_main")
    private String addressMain;
    @Column(name = "address_sub")
    private String addressSub;

    private String contact;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
