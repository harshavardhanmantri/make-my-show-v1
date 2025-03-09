package com.example.makemyshow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private List<String> roles;
}
