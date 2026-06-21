package com.instrumentroom.dto.auth;

import lombok.*;

/**
 * 用户登录响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;
}
