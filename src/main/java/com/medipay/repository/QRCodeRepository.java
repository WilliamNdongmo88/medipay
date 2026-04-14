package com.medipay.repository;

import com.medipay.entity.QRCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    Optional<QRCode> findByCodeValue(String codeValue);

    /**
     * Met à jour l'état d'utilisation d'un QR Code par sa valeur unique.
     * @param codeValue La valeur du QR Code (ex: UUID ou chaîne générée)
     * @param isUsed Le nouvel état (true pour utilisé)
     * @return Le nombre de lignes modifiées (doit être 1)
     */
    @Modifying
    @Transactional
    @Query("UPDATE QRCode q SET q.isUsed = :isUsed WHERE q.codeValue = :codeValue")
    int updateIsUsedStatus(@Param("codeValue") String codeValue, @Param("isUsed") boolean isUsed);
}
