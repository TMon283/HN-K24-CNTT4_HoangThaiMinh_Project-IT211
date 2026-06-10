package com.badminton.dto.response;

import com.badminton.constant.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private boolean enabled;
    private Set<RoleType> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
