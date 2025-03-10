package com.example.makemyshow.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ScreenRequestDto {

    @NotBlank(message = "Screen name is required")
    private String name;

    @NotNull(message = "Theater ID is required")
    private Long theaterId;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(min = 1, message = "At least one seat configuration is required")
    private List<SeatConfigDto> seatConfigs;

    @Data
    public static class SeatConfigDto {
        @NotBlank(message = "Row name is required")
        private String rowName;

        @NotNull(message = "Seat count is required")
        @Min(value = 1, message = "Seat count must be at least 1")
        private Integer seatCount;

        @NotNull(message = "Seat type is required")
        private String seatType;
    }
}