package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requête de réinitialisation de mot de pass")
public class ResetPasswordRequest {
    @Schema(example = "will@gmail.com")
    private String email;
    @Schema(example = "will123")
    private String newPassword;
}
