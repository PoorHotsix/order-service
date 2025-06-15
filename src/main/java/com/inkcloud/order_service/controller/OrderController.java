package com.inkcloud.order_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderMemberDto;
import com.inkcloud.order_service.dto.OrderReviewDto;
import com.inkcloud.order_service.dto.UpdateOrdersRequestDto;
import com.inkcloud.order_service.dto.common.OrderSimpleResponseDto;
import com.inkcloud.order_service.dto.event.OrderEventDto;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;
    
    @PostMapping
    public ResponseEntity<OrderEventDto> createOrder(@RequestBody OrderDto dto, @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>( service.createOrder(dto,jwt),HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<OrderDto> getOrderDetail(@RequestParam(value = "order_id") String orderId, @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(service.retriveOrder(orderId, jwt), HttpStatus.OK);
    }

    @GetMapping("/member")
    public ResponseEntity<Page<OrderMemberDto>> getMemberOrders(@AuthenticationPrincipal Jwt jwt, @RequestParam String state, @ModelAttribute OrderDateCreteria date, @ModelAttribute OrderSortingCreteria sort, Pageable page) {
        Page<OrderMemberDto> pages = service.retriveOrdersByMember(jwt, state, date, sort, page);
        return new ResponseEntity<>(pages, HttpStatus.OK);
    }
    @GetMapping("/member/ship")
    public ResponseEntity<Page<OrderReviewDto>> getMemberOrdersInShipped(@AuthenticationPrincipal Jwt jwt, @ModelAttribute OrderDateCreteria date, @ModelAttribute OrderSortingCreteria sort, Pageable page) {
        Page<OrderReviewDto> pages = service.retriveOrdersByMemberInShipped(jwt, date, sort, page);
        return new ResponseEntity<>(pages, HttpStatus.OK);
    }
    
    @GetMapping("/all")    
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(@ModelAttribute OrderSearchCreteria search, @ModelAttribute OrderDateCreteria date, @ModelAttribute OrderSortingCreteria sort, Pageable page) {
        Page<OrderDto> pages = service.allRetriveOrders(search, date, sort, page);
        return new ResponseEntity<>(pages, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<OrderDto> cancelOrder(@RequestParam(value = "order_id") String orderId, @AuthenticationPrincipal Jwt jwt){
        return new ResponseEntity<>(service.cancleOrder(orderId, jwt), HttpStatus.ACCEPTED);
    }
    
    @PatchMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<OrderDto> updateOrder(@RequestParam(value = "order_id") String orderId,@RequestParam(value = "state") OrderState state, @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(service.updateOrder(orderId, state, jwt), HttpStatus.ACCEPTED);
    }

    // @PatchMapping("/all")
    // @PreAuthorize("hasAuthority('ADMIN')")
    // public ResponseEntity<List<OrderSimpleResponseDto>> updateOrder(@RequestParam(value = "order_id") List<String> orderId, @AuthenticationPrincipal Jwt jwt) {
    //     return new ResponseEntity<>(service.updateOrder(orderId, jwt), HttpStatus.ACCEPTED);
    // }
    @PatchMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<OrderDto>> updateOrder(@RequestBody UpdateOrdersRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(service.updateOrder(request, jwt), HttpStatus.ACCEPTED);
    }

    // @PatchMapping("/all")
    // @PreAuthorize("hasAuthority('ADMIN')")
    // public ResponseEntity<List<OrderSimpleResponseDto>> cancelAllOrder(@RequestParam(value = "order_id") List<String> orderId, @AuthenticationPrincipal Jwt jwt) {
    //     return new ResponseEntity<>(service.cancleOrder(orderId, jwt), HttpStatus.ACCEPTED);
    // }
    
    

}
