package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Requête pour générer qr-code")
public class QRCodeRequest {
    @Schema(example = "1250")
    private BigDecimal amount;
}
