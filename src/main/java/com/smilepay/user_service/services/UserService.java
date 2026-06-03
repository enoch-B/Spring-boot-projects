package com.smilepay.user_service.services;

import com.smilepay.user_service.dto.RequestDto.LoginRequest;
import com.smilepay.user_service.dto.RequestDto.TransferRequest;
import com.smilepay.user_service.dto.RequestDto.UserRequest;
import com.smilepay.user_service.dto.ResponseDto.BalanceResponse;
import com.smilepay.user_service.dto.ResponseDto.TransferResponse;
import com.smilepay.user_service.dto.ResponseDto.UserResponse;
import com.smilepay.user_service.enums.UserStatus;
import com.smilepay.user_service.exception.ResourceNotFoundException;
import com.smilepay.user_service.mapper.UserMapper;
import com.smilepay.user_service.model.User;
import com.smilepay.user_service.model.Wallet;
import com.smilepay.user_service.repository.UserRepository;
import com.smilepay.user_service.repository.WalletRepository;
import com.smilepay.user_service.util.PinUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository repository;
    private final WalletRepository walletRepository;
    private final PinUtil pinUtil;
    private final UserMapper userMapper;

     @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("Registering User: {}", request.getPhoneNumber());

        //validat pin
        if(!request.getPin().equals(request.getConfirmPin())){
            throw new IllegalArgumentException("PINs do not match");
        }
        if(repository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new IllegalArgumentException("User by this Phone number already exists");
        }
        User user = userMapper.toEntity(request);

        String salt = pinUtil.generateSalt();
        String hashedPin = pinUtil.hashPin(request.getPin(), salt);
        user.setPinHash(salt + ":" + hashedPin);


        User savedUser=repository.save(user);
        // Create wallet
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        walletRepository.save(wallet);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser, wallet);


    }

    @Transactional
    public UserResponse loginUser(LoginRequest request) {
     log.info("Attempting to login: {}",request.getPhoneNumber());
        User user=repository.findByPhoneNumber(request.getPhoneNumber())
             .orElseThrow(()->new ResourceNotFoundException("User Not Found"));

        // Verify PIN
        String[] parts = user.getPinHash().split(":");
        String salt = parts[0];
        String storedHash = parts[1];
        if(!pinUtil.verifyPin(request.getPin(), storedHash, salt)){
            throw new IllegalArgumentException("Invalid PIN");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());


        User updatedUser=repository.save(user);
            Wallet wallet=walletRepository.findByUserPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(()-> new ResourceNotFoundException("Wallet Not Found for User ID: "+updatedUser.getId()));
        log.info("User logged in successfully: {}",request.getPhoneNumber());
        return userMapper.toResponse(updatedUser, wallet);

    }

    public UserResponse getUserByPhone(String phone) {
        User user = repository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserPhoneNumber(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return userMapper.toResponse(user, wallet);
    }

    public List<UserResponse> getAllUsers() {
        return repository.findAllWithWallet()
                .stream()
                .map(user -> userMapper.toResponse(user, user.getWallet()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferResponse processTransfer(@Valid TransferRequest request) {
        log.info("Transfer from {} to {}: {}", request.getSenderPhone(), request.getReceiverPhone(), request.getAmount());

        // Get sender with lock
        Wallet senderWallet = walletRepository.findByUserPhoneForUpdate(request.getSenderPhone())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        // Get receiver
        Wallet receiverWallet = walletRepository.findByUserPhoneNumber(request.getReceiverPhone())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        User sender = senderWallet.getUser();
        String[] parts = sender.getPinHash().split(":");
        if(!pinUtil.verifyPin(request.getPin(), parts[1], parts[0])){
            throw new IllegalArgumentException("Invalid PIN");
        }

        //Process transfer

        senderWallet.debit(request.getAmount());
        receiverWallet.credit(request.getAmount());

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Create response
        TransferResponse response = new TransferResponse();
        response.setTransactionId("TXN" + System.currentTimeMillis());
        response.setSenderPhone(request.getSenderPhone());
        response.setReceiverPhone(request.getReceiverPhone());
        response.setAmount(request.getAmount());
        response.setNewBalance(senderWallet.getBalance());
        response.setStatus("SUCCESS");
        response.setMessage("Transfer completed");
        response.setTimestamp(LocalDateTime.now());

        return response;
    }

    @Transactional
    public  UserResponse creditWallet(String phoneNumber, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserPhoneForUpdate(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this phone number not found"+ phoneNumber));
        wallet.credit(amount);
        walletRepository.save(wallet);
        return userMapper.toResponse(wallet.getUser(), wallet);
    }

    @Transactional
    public UserResponse debitWallet(String phoneNumber, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserPhoneForUpdate(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        wallet.debit(amount);
        walletRepository.save(wallet);
        return userMapper.toResponse(wallet.getUser(), wallet);
    }

    public BalanceResponse getBalance(String phoneNumber, String pin){

         //verify pin
        User user= repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with this phone number"+ phoneNumber));

        String[] parts = user.getPinHash().split(":");
        String salt = parts[0];
        String storedHash = parts[1];
        if(!pinUtil.verifyPin(pin, storedHash, salt)){
            throw new IllegalArgumentException("Invalid PIN");
        }


         Wallet existingWallet = walletRepository.findByUserPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this phone number not found"+ phoneNumber));


        return BalanceResponse.builder()
                .id(existingWallet.getId())
                .phoneNumber(phoneNumber)
                .balance(existingWallet.getBalance())
                .fullName(existingWallet.getUser().getFullName())
                .walletNumber(existingWallet.getWalletNumber())
                .dailyLimit(existingWallet.getDailyLimit())
                .balance(existingWallet.getBalance())
                .dailyUsed(existingWallet.getDailyUsed())
                .build();
    }

}
