package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requête de vérification d'email")
public class VerifyEmailRequest {
    @Schema(example = "will@gmail.com")
    private String email;
}
