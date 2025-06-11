package com.inkcloud.order_service.dto.child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderItemDto {
    private String itemId;
    private String name;
    private int price;
    private int quantity;
    private String author;
    private String publisher;
    private String thumbnailUrl;
}
