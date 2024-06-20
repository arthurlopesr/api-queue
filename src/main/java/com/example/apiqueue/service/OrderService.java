package com.example.apiqueue.service;

import com.example.apiqueue.dto.OrderCreatedEventDTO;
import com.example.apiqueue.entity.OrderEntity;
import com.example.apiqueue.entity.OrderItemEntity;
import com.example.apiqueue.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void save(OrderCreatedEventDTO eventDTO) {
        var entity = new OrderEntity();
        entity.setOrderId(eventDTO.orderCode());
        entity.setCustomerId(eventDTO.clientCode());

        entity.setItems(getOrderItems(eventDTO));
        entity.setTotal(getTotal(eventDTO));

        orderRepository.save(entity);
    }

    private static List<OrderItemEntity> getOrderItems(OrderCreatedEventDTO eventDTO) {
        return eventDTO.items().stream()
                .map(item -> new OrderItemEntity(item.product(), item.quantity(), item.price()))
                .toList();
    }

    private BigDecimal getTotal(OrderCreatedEventDTO eventDTO) {
        return eventDTO.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}
