package com.smilepay.user_service.repository;

import com.smilepay.user_service.model.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletNumber(String walletNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.phoneNumber = :phone")
   Optional< Wallet> findByUserPhoneForUpdate(@Param("phone") String phone);

    Optional<Wallet> findByUserPhoneNumber(String phoneNumber);
}
