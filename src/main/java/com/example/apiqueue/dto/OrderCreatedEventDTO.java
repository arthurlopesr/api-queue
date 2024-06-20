package com.example.apiqueue.dto;

import java.util.List;

public record OrderCreatedEventDTO(Long orderCode,
                                   Long clientCode,
                                   List<OrderItemEventDTO> items) {
}
