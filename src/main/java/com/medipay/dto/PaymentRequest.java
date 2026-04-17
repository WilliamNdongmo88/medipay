package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Requête de payment")
public class PaymentRequest {
    @Schema(example = "76aedafc-674b-469b-91bc-d3965f5cdd72")
    private String qrCodeValue;
    @Schema(example = "7")
    private Long pharmacistId;
    @Schema(example = "1750")
    private BigDecimal amount;
}
