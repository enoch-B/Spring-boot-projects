package com.smilepay.user_service.dto.ResponseDto;

import com.smilepay.user_service.enums.UserStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private String walletNumber;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private BigDecimal dailyUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private  LocalDateTime lastLoginAt;


}
