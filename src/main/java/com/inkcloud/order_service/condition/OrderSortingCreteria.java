package com.inkcloud.order_service.condition;

import com.inkcloud.order_service.enums.SortDirection;
import com.inkcloud.order_service.enums.SortField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSortingCreteria {
    private SortField sortBy;
    private SortDirection sortDir;

}
