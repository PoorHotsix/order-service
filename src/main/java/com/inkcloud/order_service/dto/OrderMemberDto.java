package com.inkcloud.order_service.dto;

import java.time.LocalDateTime;
import com.inkcloud.order_service.enums.OrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderMemberDto {
    private String id;
    private OrderState state;
    private LocalDateTime createdAt;
    private int price;
    private int quantity;
    private String orderName;
    private String receiver;
    private String delegateProduct; // 대표 상품
    private int typesNum; // 종류


}
