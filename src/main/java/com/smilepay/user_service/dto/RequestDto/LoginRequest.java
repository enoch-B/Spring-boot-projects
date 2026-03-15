package com.smilepay.user_service.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^251[0-9]{9}$")
    private String phoneNumber;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String pin;
}
