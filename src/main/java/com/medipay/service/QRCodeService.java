package com.medipay.service;

import com.medipay.entity.QRCode;
import com.medipay.entity.User;
import com.medipay.repository.QRCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    private final QRCodeRepository qrCodeRepository;

    public QRCode generateQRCode(User pharmacist, BigDecimal amount) {
        QRCode qrCode = new QRCode();
        qrCode.setPharmacist(pharmacist);
        qrCode.setAmount(amount); // Peut être null si QR statique
        qrCode.setCodeValue(UUID.randomUUID().toString());
        qrCode.setUsed(false);

        return qrCodeRepository.save(qrCode);
    }

    public QRCode validateAndGetQRCode(String codeValue) {
        QRCode qrCode = qrCodeRepository.findByCodeValue(codeValue)
                .orElseThrow(() -> new RuntimeException("QR Code invalide ou inexistant"));

        if (qrCode.isUsed()) {
            throw new RuntimeException("Ce QR Code a déjà été utilisé");
        }

        return qrCode;
    }
}

