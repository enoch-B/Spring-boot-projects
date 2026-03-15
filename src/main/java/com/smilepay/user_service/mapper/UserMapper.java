package com.smilepay.user_service.mapper;

import com.smilepay.user_service.dto.RequestDto.TransferRequest;
import com.smilepay.user_service.dto.RequestDto.UserRequest;
import com.smilepay.user_service.dto.ResponseDto.TransferResponse;
import com.smilepay.user_service.dto.ResponseDto.UserResponse;
import com.smilepay.user_service.model.User;
import com.smilepay.user_service.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public User toEntity(UserRequest request){
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        return user;
    }

    public UserResponse toResponse(User user, Wallet wallet){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if(wallet != null){
            response.setWalletNumber(wallet.getWalletNumber());
            response.setBalance(wallet.getBalance());
            response.setDailyLimit(wallet.getDailyLimit());
            response.setDailyUsed(wallet.getDailyUsed());

        }

        return response;
    }
    public TransferResponse toTransferResponse(User user, TransferRequest request, Wallet wallet){
        TransferResponse response = new TransferResponse();
        response.setTransactionId("TXN" + System.currentTimeMillis());
        response.setSenderPhone(request.getSenderPhone());
        response.setReceiverPhone(request.getReceiverPhone());
        response.setAmount(request.getAmount());
        response.setNewBalance(wallet.getBalance());
        response.setStatus("SUCCESS");
        response.setMessage("Transfer completed");
        response.setTimestamp(LocalDateTime.now());

        return response;
    }
// public User mapUpdateBalance(User user, Wallet wallet, TransferRequest request){
//        if ()
// }
}
