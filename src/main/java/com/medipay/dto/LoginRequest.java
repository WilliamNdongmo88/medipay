package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requête de connexion")
public class LoginRequest {

    @Schema(example = "williamndongmo")
    private String username;

    @Schema(example = "admin123")
    private String password;
}
