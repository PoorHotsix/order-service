package com.inkcloud.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShipDto {
    private String name;
    private String receiver;
    private int zipcode;

    private String addressMain;
    private String addressSub;
    private String contact;
}
