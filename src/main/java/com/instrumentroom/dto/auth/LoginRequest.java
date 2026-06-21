package com.instrumentroom.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 用户登录请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;

    @NotBlank(message = "密码不能为空")
    private String password;
}
