package com.example.makemyshow.dto.response;

import lombok.Data;

@Data
public class SeatResponseDto {
    private Long id;
    private String rowName;
    private String seatNumber;
    private String seatType;
    private Double price;
}