package com.example.apiqueue.dto;

import java.math.BigDecimal;

public record OrderItemEventDTO(String product,
                                Integer quantity,
                                BigDecimal price) {
}
