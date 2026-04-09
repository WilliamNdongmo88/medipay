package com.medipay.controller;

import com.medipay.dto.QRCodeRequest;
import com.medipay.entity.QRCode;
import com.medipay.entity.User;
import com.medipay.repository.UserRepository;
import com.medipay.service.QRCodeService;
import com.medipay.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qrcode" )
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACIST')")
public class QRCodeController {
    private final QRCodeService qrCodeService;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateQRCode(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody QRCodeRequest qrCodeRequest) {

        User pharmacist = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Pharmacien non trouvé"));

        QRCode qrCode = qrCodeService.generateQRCode(pharmacist, qrCodeRequest.getAmount());

        return ResponseEntity.ok(Map.of("qrCodeValue", qrCode.getCodeValue(), "amount", qrCode.getAmount()));
    }
}
