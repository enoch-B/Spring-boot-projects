package com.smilepay.user_service.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE,
    PENDING,
    SUSPENDED,
    BLOCKED,
    KYC_VERIFICATION_PENDING,
}
