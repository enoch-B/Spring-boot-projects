package com.smilepay.user_service.dto.ResponseDto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private String transactionId;
    private String senderPhone;
    private String receiverPhone;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
