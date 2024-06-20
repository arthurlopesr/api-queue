package com.example.apiqueue.dto;

import java.util.List;

public record ApiResponseDTO<T>(List<T> data, PaginationResponseDTO pagination) {
}
