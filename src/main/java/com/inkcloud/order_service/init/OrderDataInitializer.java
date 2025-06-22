package com.inkcloud.order_service.init;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.inkcloud.order_service.domain.MemberInfo;
import com.inkcloud.order_service.domain.Order;
import com.inkcloud.order_service.domain.OrderItem;
import com.inkcloud.order_service.domain.OrderShip;
import com.inkcloud.order_service.enums.OrderState;
import com.inkcloud.order_service.enums.PaymentMethod;
import com.inkcloud.order_service.repository.OrderRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataInitializer {
    private final OrderRepository repo;

    @PostConstruct
    @Transactional
    public void initOrderDatas() {
        try {
            log.info("주문 데이터 초기화 시작...");

            // 기존 데이터 확인
            if (repo.count() > 0) {
                log.info("기존 주문 데이터가 존재합니다. 초기화를 건너뜁니다.");
                return;
            }

            importOrderDataFromCsv();

            log.info("주문 데이터 초기화 완료!");

        } catch (Exception e) {
            log.error("주문 데이터 초기화 중 오류 발생: ", e);
        }
    }

    private void importOrderDataFromCsv() throws Exception {
        // CSV 파일들을 동시에 읽어서 Order 객체 구성
        Map<String, Order> orderMap = loadOrdersFromCsv();
        Map<String, List<OrderItem>> orderItemsMap = loadOrderItemsFromCsv();
        Map<String, OrderShip> orderShipsMap = loadOrderShipsFromCsv();

        // Order 객체들을 완성하여 저장
        for (String orderId : orderMap.keySet()) {
            Order order = orderMap.get(orderId);

            // OrderItems 설정
            List<OrderItem> items = orderItemsMap.get(orderId);
            if (items != null) {
                for (OrderItem item : items) {
                    item.setOrder(order); // FK 관계 설정
                }
                order.setOrderItems(items);
            }

            // OrderShip 설정
            OrderShip ship = orderShipsMap.get(orderId);
            if (ship != null) {
                ship.setOrder(order); // FK 관계 설정
                order.setOrderShip(ship);
            }

            repo.save(order); // Cascade로 연관 엔티티들도 함께 저장
        }

        log.info("총 {}개의 주문 데이터를 저장했습니다.", orderMap.size());
    }

    private Map<String, Order> loadOrdersFromCsv() throws Exception {
        Map<String, Order> orderMap = new HashMap<>();
        Resource resource = new ClassPathResource("data/orders.csv");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵

            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);

                Order order = new Order();
                order.setId(fields[0]);
                order.setState(OrderState.valueOf(fields[1]));
                order.setCreatedAt(LocalDateTime.parse(fields[2],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                order.setUpdatedAt(LocalDateTime.parse(fields[3],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                order.setPrice(Integer.parseInt(fields[4]));
                order.setQuantity(Integer.parseInt(fields[5]));
                order.setPaymentMethod(PaymentMethod.valueOf(fields[6]));
                order.setShippingFee(Integer.parseInt(fields[7]));

                // MemberInfo 설정
                MemberInfo memberInfo = new MemberInfo();
                memberInfo.setMemberEmail(fields[8]);
                memberInfo.setMemberContact(fields[9]);
                memberInfo.setMemberName(fields[10]);
                order.setMember(memberInfo);

                orderMap.put(order.getId(), order);
            }
        }

        return orderMap;
    }

    private Map<String, List<OrderItem>> loadOrderItemsFromCsv() throws Exception {
        Map<String, List<OrderItem>> orderItemsMap = new HashMap<>();
        Resource resource = new ClassPathResource("data/order_items.csv");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵

            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);

                OrderItem item = new OrderItem();
                item.setItemId(Long.parseLong(fields[1]));
                item.setName(fields[2]);
                item.setPrice(Integer.parseInt(fields[3]));
                item.setQuantity(Integer.parseInt(fields[4]));
                item.setAuthor(fields[5]);
                item.setPublisher(fields[6]);
                item.setThumbnailUrl(fields[7]);

                String orderId = fields[0];
                orderItemsMap.computeIfAbsent(orderId, k -> new ArrayList<>()).add(item);
            }
        }

        return orderItemsMap;
    }

    private Map<String, OrderShip> loadOrderShipsFromCsv() throws Exception {
        Map<String, OrderShip> orderShipsMap = new HashMap<>();
        Resource resource = new ClassPathResource("data/order_ships.csv");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵

            while ((line = reader.readLine()) != null) {
                String[] fields = parseCSVLine(line);

                OrderShip ship = new OrderShip();
                ship.setName(fields[1]);
                ship.setReceiver(fields[2]);
                ship.setZipcode(Integer.valueOf(fields[3]));
                ship.setAddressMain(fields[4]);
                ship.setAddressSub(fields[5]);
                ship.setContact(fields[6]);

                String orderId = fields[0];
                orderShipsMap.put(orderId, ship);
            }
        }

        return orderShipsMap;
    }

    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

}
