package com.smilepay.user_service.controller;

import com.smilepay.user_service.dto.RequestDto.LoginRequest;
import com.smilepay.user_service.dto.RequestDto.TransferRequest;
import com.smilepay.user_service.dto.RequestDto.UserRequest;
import com.smilepay.user_service.dto.ResponseDto.BalanceResponse;
import com.smilepay.user_service.dto.ResponseDto.TransferResponse;
import com.smilepay.user_service.dto.ResponseDto.UserResponse;
import com.smilepay.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {
     private final UserService userService;

     Long start=System.currentTimeMillis();

     @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest request){
         UserResponse response= userService.registerUser(request);

         return new ResponseEntity<>(response, HttpStatus.CREATED);
     }
     @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@RequestBody LoginRequest request){
         UserResponse response= userService.loginUser(request);

         return ResponseEntity.ok(response);
     }

     @GetMapping("/{phoneNumber}")
    public ResponseEntity<UserResponse> getUserByPhone(@PathVariable String phoneNumber){
         UserResponse response= userService.getUserByPhone(phoneNumber);

         return ResponseEntity.ok(response);
     }
    @GetMapping("/get-all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

         log.info("Time taken to fetch all users: {} ms", System.currentTimeMillis() - start);
         return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request){
         return ResponseEntity.ok(userService.processTransfer(request));
    }

    @PutMapping("{phoneNumber}/credit")
    public ResponseEntity<UserResponse> creditWallet(@PathVariable String phoneNumber, @RequestBody BigDecimal amount){
         return ResponseEntity.ok(userService.creditWallet(phoneNumber, amount));
    }

     @PutMapping("{phoneNumber}/debit")
    public ResponseEntity<UserResponse> debitWallet(@PathVariable String phoneNumber, @RequestParam BigDecimal amount){
         return ResponseEntity.ok(userService.debitWallet(phoneNumber, amount));
     }

     @GetMapping("/{pin}/{phone}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String phone, @PathVariable String pin){
         return ResponseEntity.ok(userService.getBalance(phone, pin));
     }
}
