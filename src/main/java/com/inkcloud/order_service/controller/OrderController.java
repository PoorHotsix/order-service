package com.inkcloud.order_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inkcloud.order_service.condition.OrderDateCreteria;
import com.inkcloud.order_service.condition.OrderSearchCreteria;
import com.inkcloud.order_service.condition.OrderSortingCreteria;
import com.inkcloud.order_service.dto.OrderDto;
import com.inkcloud.order_service.dto.OrderEventDto;
import com.inkcloud.order_service.dto.OrderSimpleResponseDto;
import com.inkcloud.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

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
import org.springframework.web.bind.annotation.PathVariable;




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
    public ResponseEntity<OrderDto> getOrderDetail(@RequestParam(value = "order_id") String orderId) {
        return new ResponseEntity<>(service.retriveOrder(orderId), HttpStatus.OK);
    }

    @GetMapping("/member")
    public ResponseEntity<Page<OrderDto>> getMemberOrders(@RequestParam(value = "member_id") String memberId, @ModelAttribute OrderDateCreteria date, @ModelAttribute OrderSortingCreteria sort, Pageable page) {
        Page<OrderDto> pages = service.retriveOrdersByMember(memberId, date, sort, page);
        return new ResponseEntity<>(pages, HttpStatus.OK);
    }
    
    @GetMapping("/all")    
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(@ModelAttribute OrderSearchCreteria search, @ModelAttribute OrderDateCreteria date, @ModelAttribute OrderSortingCreteria sort, Pageable page) {
        Page<OrderDto> pages = service.allRetriveOrders(search, date, sort, page);
        return new ResponseEntity<>(pages, HttpStatus.OK);
    }

    @PatchMapping("/{order_id}")
    public ResponseEntity<OrderSimpleResponseDto> cancelOrder(@PathVariable(name = "order_id") String orderId, @AuthenticationPrincipal Jwt jwt){
        return new ResponseEntity<>(service.cancleOrder(orderId, jwt), HttpStatus.ACCEPTED);
    }
    
    @PutMapping("/{order_id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<OrderSimpleResponseDto> updateOrder(@PathVariable(name = "order_id") String orderId, @AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(service.updateOrder(orderId, jwt), HttpStatus.ACCEPTED);
    }
    
    

}
