package com.medipay.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.medipay.entity.QRCode;
import com.medipay.entity.User;
import com.medipay.repository.QRCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public byte[] generateDynamicQrCode(String content, int width, int height)
            throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}

