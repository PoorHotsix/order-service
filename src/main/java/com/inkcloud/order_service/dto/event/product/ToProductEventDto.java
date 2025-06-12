package com.inkcloud.order_service.dto.event.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToProductEventDto {
    Long productId;
    int deltaQuantity;
}
