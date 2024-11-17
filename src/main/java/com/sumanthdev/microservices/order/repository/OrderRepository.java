package com.sumanthdev.microservices.order.repository;

import com.sumanthdev.microservices.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
