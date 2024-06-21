package com.example.apiqueue.service;

import com.example.apiqueue.dto.OrderCreatedEventDTO;
import com.example.apiqueue.dto.OrderResponseDTO;
import com.example.apiqueue.entity.OrderEntity;
import com.example.apiqueue.entity.OrderItemEntity;
import com.example.apiqueue.repository.OrderRepository;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(OrderCreatedEventDTO eventDTO) {
        var entity = new OrderEntity();
        entity.setOrderId(eventDTO.orderCode());
        entity.setCustomerId(eventDTO.clientCode());

        entity.setItems(getOrderItems(eventDTO));
        entity.setTotal(getTotal(eventDTO));

        orderRepository.save(entity);
    }

    public Page<OrderResponseDTO> findAllByCustomerId(Long customerId, PageRequest pageRequest) {
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        return orders.map(OrderResponseDTO::fromEntity);
    }

    public BigDecimal findTotalOnOrderByCustomerId(Long customerId) {
        var aggregations = newAggregation(
                match(Criteria.where("customerId").is(customerId)),
                group().sum("total").as("total")
        );

        var response = mongoTemplate.aggregate(aggregations, "orders", Document.class);
        return new BigDecimal(response.getUniqueMappedResult().get("total").toString());
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
