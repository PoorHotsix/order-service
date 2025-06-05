package com.inkcloud.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDto {
    private String name;
    private int price;
    private int quantity;
    private String auther;
    private String publisher;
    private String thumbnailUrl;
}
