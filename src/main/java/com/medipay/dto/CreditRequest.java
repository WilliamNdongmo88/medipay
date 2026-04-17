package com.medipay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Requête de dépot")
public class CreditRequest {

    @Schema(example = "2")
    private Long userId;

    @Schema(example = "3500")
    private BigDecimal amount;
}
