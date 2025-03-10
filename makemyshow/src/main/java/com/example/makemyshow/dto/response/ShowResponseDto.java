package com.example.makemyshow.dto.response;

import com.example.makemyshow.model.Show;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ShowResponseDto {
    private Long id;
    private MovieResponseDto movie;
    private TheaterResponseDto theater;
    private String screenName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Double> seatPrices;
    private boolean isActive;

    public static ShowResponseDto fromShow(Show show) {
        ShowResponseDto dto = new ShowResponseDto();
        dto.setId(show.getId());
        dto.setMovie(MovieResponseDto.fromMovie(show.getMovie()));

        TheaterResponseDto theaterDto = new TheaterResponseDto();
        theaterDto.setId(show.getScreen().getTheater().getId());
        theaterDto.setName(show.getScreen().getTheater().getName());
        theaterDto.setAddress(show.getScreen().getTheater().getAddress());
        theaterDto.setCity(show.getScreen().getTheater().getCity());
        theaterDto.setState(show.getScreen().getTheater().getState());
        theaterDto.setPincode(show.getScreen().getTheater().getPincode());
        dto.setTheater(theaterDto);

        dto.setScreenName(show.getScreen().getName());
        dto.setStartTime(show.getStartTime());
        dto.setEndTime(show.getEndTime());

        Map<String, Double> seatPrices = new HashMap<>();
        show.getSeatPrices().forEach((type, price) -> {
            seatPrices.put(type.name(), price);
        });
        dto.setSeatPrices(seatPrices);

        dto.setActive(show.isActive());

        return dto;
    }
}
