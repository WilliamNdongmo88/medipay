package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requête de refresh-token")
public class RefreshRequest {
    @Schema(example = "f45de71b-b197-4d09-a457-716de559e2db")
    private String refreshToken;
}
