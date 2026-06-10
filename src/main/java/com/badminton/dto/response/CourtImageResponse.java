package com.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtImageResponse {

    private Long id;
    private Long courtId;
    private String imageUrl;
    private String publicId;
    private LocalDateTime createdAt;
}
