package com.sumanthdev.microservices.order.service;

import com.sumanthdev.microservices.order.client.InventoryClient;
import com.sumanthdev.microservices.order.dto.OrderRequest;
import com.sumanthdev.microservices.order.event.OrderPlacedEvent;
import com.sumanthdev.microservices.order.model.Order;
import com.sumanthdev.microservices.order.repository.OrderRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);

    public void placeOrder(OrderRequest orderRequest) {
        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        if (isProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price().multiply(BigDecimal.valueOf(orderRequest.quantity())));
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);
            log.info("Product created successfully");

            var orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());
            log.info("ordernumber={} ;orderemail={} ;orderfirstname={} ;orderlastname={}", order.getOrderNumber(), orderRequest.userDetails().email(), orderRequest.userDetails().firstName(), orderRequest.userDetails().lastName());



            log.info("Start- Sending OrderPlacedEvent {} to Kafka Topic", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End- Sending OrderPlacedEvent {} to Kafka Topic", orderPlacedEvent);


        } else {
            throw new RuntimeException("Product with SkuCode " + orderRequest.skuCode() + " is not in stock");
        }
    }
}
