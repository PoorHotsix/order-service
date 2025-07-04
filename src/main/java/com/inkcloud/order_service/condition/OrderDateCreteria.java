package com.inkcloud.order_service.condition;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDateCreteria {
    private LocalDate startDate;
    private LocalDate endDate;
}
