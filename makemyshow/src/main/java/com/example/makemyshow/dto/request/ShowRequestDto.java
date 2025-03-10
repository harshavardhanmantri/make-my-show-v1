package com.example.makemyshow.dto.request;



import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ShowRequestDto {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "Seat prices are required")
    private Map<String, @Positive(message = "Price must be positive") Double> seatPrices;
}
