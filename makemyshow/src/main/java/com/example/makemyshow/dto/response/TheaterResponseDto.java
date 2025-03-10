package com.example.makemyshow.dto.response;


import com.example.makemyshow.model.Theater;
import lombok.Data;

@Data
public class TheaterResponseDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactNumber;
    private boolean isActive;

    public static TheaterResponseDto fromTheater(Theater theater) {
        TheaterResponseDto dto = new TheaterResponseDto();
        dto.setId(theater.getId());
        dto.setName(theater.getName());
        dto.setAddress(theater.getAddress());
        dto.setCity(theater.getCity());
        dto.setState(theater.getState());
        dto.setPincode(theater.getPincode());
        dto.setContactNumber(theater.getContactNumber());
        dto.setActive(theater.isActive());
        return dto;
    }
}

