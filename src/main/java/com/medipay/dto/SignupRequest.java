package com.medipay.dto;

import com.medipay.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(description = "Requête d'inscription")
public class SignupRequest {
    @Schema(example = "soh_mathurin")
    private String username;
    @Schema(example = "soh_mathurin@gmail.com")
    private String email;
    @Schema(example = "soh123")
    private String password;
    @Schema(example = "ROLE_CLIENT")
    private Role role;

}