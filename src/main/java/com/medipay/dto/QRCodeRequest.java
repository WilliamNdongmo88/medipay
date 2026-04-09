package com.medipay.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QRCodeRequest {
    private BigDecimal amount;
}
