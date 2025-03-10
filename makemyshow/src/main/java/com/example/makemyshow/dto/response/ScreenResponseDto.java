package com.example.makemyshow.dto.response;


import com.example.makemyshow.model.Screen;
import com.example.makemyshow.model.Seat;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ScreenResponseDto {
    private Long id;
    private String name;
    private Long theaterId;
    private String theaterName;
    private Integer capacity;
    private List<SeatRowDto> seatRows;
    private boolean isActive;

    @Data
    public static class SeatRowDto {
        private String rowName;
        private List<SeatDto> seats;
    }

    @Data
    public static class SeatDto {
        private Long id;
        private String seatNumber;
        private String seatType;
    }

    public static ScreenResponseDto fromScreen(Screen screen) {
        ScreenResponseDto dto = new ScreenResponseDto();
        dto.setId(screen.getId());
        dto.setName(screen.getName());
        dto.setTheaterId(screen.getTheater().getId());
        dto.setTheaterName(screen.getTheater().getName());
        dto.setCapacity(screen.getCapacity());
        dto.setActive(screen.isActive());

        // Group seats by row
        Map<String, List<Seat>> seatsByRow = screen.getSeats().stream()
                .collect(Collectors.groupingBy(Seat::getRowName));

        // Convert to DTO
        List<SeatRowDto> seatRows = seatsByRow.entrySet().stream()
                .map(entry -> {
                    SeatRowDto rowDto = new SeatRowDto();
                    rowDto.setRowName(entry.getKey());

                    List<SeatDto> seatDtos = entry.getValue().stream()
                            .map(seat -> {
                                SeatDto seatDto = new SeatDto();
                                seatDto.setId(seat.getId());
                                seatDto.setSeatNumber(seat.getSeatNumber());
                                seatDto.setSeatType(seat.getSeatType().name());
                                return seatDto;
                            })
                            .collect(Collectors.toList());

                    rowDto.setSeats(seatDtos);
                    return rowDto;
                })
                .collect(Collectors.toList());

        dto.setSeatRows(seatRows);

        return dto;
    }
}