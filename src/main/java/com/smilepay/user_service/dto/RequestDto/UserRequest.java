package com.smilepay.user_service.dto.RequestDto;

import com.smilepay.user_service.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "phone is required")
    private String phoneNumber;

    @NotNull(message = " name is required")
    private String fullName;

    private String email;


    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4-6 digits")
    private String pin;

    @NotBlank(message = "Confirm PIN is required")
    private String confirmPin;


}
