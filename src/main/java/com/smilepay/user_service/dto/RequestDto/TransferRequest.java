package com.smilepay.user_service.dto.RequestDto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

    @Data
    public class TransferRequest {

        @NotBlank(message = "Sender phone is required")
        private String senderPhone;

        @NotBlank(message = "Receiver phone is required")
        private String receiverPhone;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Minimum transfer is 1 ETB")
        private BigDecimal amount;

        @NotBlank(message = "PIN is required")
        private String pin;

        private String description;
    }

