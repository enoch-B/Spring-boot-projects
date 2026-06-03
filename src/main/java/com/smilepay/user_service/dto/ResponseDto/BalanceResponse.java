package com.smilepay.user_service.dto.ResponseDto;

import com.smilepay.user_service.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class BalanceResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String walletNumber;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private BigDecimal dailyUsed;
}
