package com.smilepay.user_service.service;

import com.smilepay.user_service.dto.RequestDto.LoginRequest;
import com.smilepay.user_service.dto.RequestDto.TransferRequest;
import com.smilepay.user_service.dto.RequestDto.UserRequest;
import com.smilepay.user_service.dto.ResponseDto.TransferResponse;
import com.smilepay.user_service.dto.ResponseDto.UserResponse;
import com.smilepay.user_service.exception.ResourceNotFoundException;
import com.smilepay.user_service.model.User;
import com.smilepay.user_service.model.Wallet;
import com.smilepay.user_service.mapper.UserMapper;
import com.smilepay.user_service.repository.UserRepository;
import com.smilepay.user_service.repository.WalletRepository;
import com.smilepay.user_service.services.UserService;
import com.smilepay.user_service.util.PinUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PinUtil pinUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserRequest validRequest;
    private UserRequest validRequest2;
    private User testUser;
    private User testUser2;
    private Wallet testWallet;
    private Wallet testWallet2;
    private UserResponse testResponse;
    private LoginRequest loginRequest;
    private TransferRequest transferRequest;
    private TransferResponse transferResponse;


    @BeforeEach
    void setUp() {
        // Create test data
        validRequest = new UserRequest();
        validRequest.setPhoneNumber("251911223366");
        validRequest.setFullName("Test User");
        validRequest.setEmail("test@email.com");
        validRequest.setPin("1234");
        validRequest.setConfirmPin("1234");

        //login request
        loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("251911223344");
        loginRequest.setPin("1234");

        testUser = new User();
        testUser.setId(1L);
        testUser.setPhoneNumber("251911223366");
        testUser.setFullName("Test User");
        testUser.setPinHash("testSalt:hashed-pin");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setPhoneNumber("251911223344");
        testUser2.setFullName("Receiver User");
        testUser2.setPinHash("testSalt:hashed-pin2");


        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUser(testUser);
        testWallet.setBalance(new BigDecimal("1000.00"));

        testWallet2 = new Wallet();
        testWallet2.setId(2L);
        testWallet2.setUser(testUser2);
        testWallet2.setBalance(new BigDecimal("500.00"));



        testResponse = new UserResponse();
        testResponse.setId(1L);
        testResponse.setPhoneNumber("251911223366");
        testResponse.setFullName("Test User");

        transferRequest = new TransferRequest();
        transferRequest.setSenderPhone("251911223366");
        transferRequest.setPin("1234");
        transferRequest.setReceiverPhone("251911223344");
        transferRequest.setAmount(new BigDecimal("2000.00"));
        transferRequest.setDescription("Test transfer");


        transferResponse = new TransferResponse();
        transferResponse.setNewBalance(new BigDecimal("900.00"));
        transferResponse.setTransactionId("txn-12345");
        transferResponse.setSenderPhone("251911223366");
        transferResponse.setReceiverPhone("251911223344");
        transferResponse.setAmount(new BigDecimal("100.00"));
        transferResponse.setStatus("SUCCESS");
        transferResponse.setMessage("Transfer completed");
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange - tell mocks what to do
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(pinUtil.generateSalt()).thenReturn("test-salt");
        when(pinUtil.hashPin(anyString(), anyString())).thenReturn("hashed-pin");
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(userMapper.toResponse(any(User.class), any(Wallet.class))).thenReturn(testResponse);

        // Act
        UserResponse response = userService.registerUser(validRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("251911223344", response.getPhoneNumber());

        // Verify interactions
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));

        System.out.println("✅ Registration test passed!");
    }


    @Test
    @DisplayName(("Should throw an error when trying to register with an existing phone number"))
    void testRegisterUser_PhoneNumberExists(){
        //arrange
        when(userRepository.existsByPhoneNumber("251911223366")).thenReturn(true);

        //act and assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(validRequest)
        );
        assertEquals("User by this Phone number already exists", exception.getMessage());


    }
    @Test
    @DisplayName("Should throw an error when PIN and confirm PIN do not match")
    void testRegisterUser_PinMismatch(){
        //arrange
        validRequest.setConfirmPin("4321");

        //act and assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(validRequest)
        );
        assertEquals("PINs do not match", exception.getMessage());
        verify(userRepository, never()).save(any());

    }


    @Test
    @DisplayName("Should login successfully with correct phone and PIN")
    void testUserLogin_success(){
        //arrange
        when(userRepository.findByPhoneNumber("251911223344")).thenReturn(Optional.of(testUser));
        when(pinUtil.verifyPin("1234", "hashed-pin", "testSalt")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.findByUserPhoneNumber("251911223344")).thenReturn(Optional.of(testWallet));
        when(userMapper.toResponse(testUser, testWallet)).thenReturn(testResponse);

        //act
        UserResponse response = userService.loginUser(loginRequest);

        //assert
        assertNotNull(response, "Response should not be null");
        assertEquals("251911223344", response.getPhoneNumber());

        // Verify interactions
        verify(userRepository).findByPhoneNumber("251911223344");
        verify(pinUtil).verifyPin("1234", "hashed-pin", "testSalt");
        verify(userRepository).save(any(User.class));
        verify(walletRepository).findByUserPhoneNumber("251911223344");
        // ✅ Removed: verify(walletRepository).save(any(Wallet.class));

        assertNotNull(testUser.getLastLoginAt(), "Last login time should be set");
        System.out.println("✅ Login test passed!");
    }

    @Test
    @DisplayName("Throw an error when user is not found")
    void testLogin_UserNotfound(){
        //arrange
        when(userRepository.findByPhoneNumber("251911223344")).thenReturn(Optional.empty());

        //act and assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.loginUser(loginRequest)
        );

        // ✅ Fixed: Match actual error message
        assertEquals("User Not Found", exception.getMessage());

        // Verify no interactions
        verify(pinUtil, never()).verifyPin(any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(walletRepository, never()).findByUserPhoneNumber(any());
    }

    @Test
    @DisplayName("Should throw an error upon login with incorrect PIN")
    void testUserLogin_incorrectPin(){
        //arrange
        when(userRepository.findByPhoneNumber("251911223344")).thenReturn(Optional.of(testUser));
        when(pinUtil.verifyPin("1234", "hashed-pin", "testSalt")).thenReturn(false);

        //act & assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(loginRequest)
        );

        //  Fixed: Match actual error message
        assertEquals("Invalid PIN", exception.getMessage());

        // Verify interactions
        verify(userRepository).findByPhoneNumber("251911223344");
        verify(pinUtil).verifyPin("1234", "hashed-pin", "testSalt");
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).findByUserPhoneNumber(anyString());
    }

    @Test
    @DisplayName("should get a user by phone number")
    void testGetUserByPhoneNumber_success(){
        //arrange
        when(userRepository.findByPhoneNumber("251911223344")).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserPhoneNumber("251911223344")).thenReturn(Optional.of(testWallet));
        when(userMapper.toResponse(testUser, testWallet)).thenReturn(testResponse);

        //act
        UserResponse response = userService.getUserByPhone("251911223344");

        //assert
        assertNotNull(response, "Response should not be null");
        assertEquals("251911223344", response.getPhoneNumber());
        //verify interactions
        verify(userRepository).findByPhoneNumber("251911223344");
        verify(walletRepository).findByUserPhoneNumber("251911223344");

        System.out.println(" Get user by phone number test passed!");
    }

    @Test
    @DisplayName("Should process a successful transfer")
    void testProcessTransfer_success(){
        // arrange

        // Debug: print the request values
        System.out.println("Sender: " + transferRequest.getSenderPhone());
        System.out.println("Receiver: " + transferRequest.getReceiverPhone());
        System.out.println("Amount: " + transferRequest.getAmount());

        when(walletRepository.findByUserPhoneForUpdate("251911223366")).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByUserPhoneNumber("251911223344")).thenReturn(Optional.of(testWallet2));
        when(pinUtil.verifyPin("1234", "hashed-pin", "testSalt")).thenReturn(true);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);
        when(walletRepository.save(testWallet2)).thenReturn(testWallet2);

        //initial balance
        BigDecimal senderInitialBalance = testWallet.getBalance();
        BigDecimal receiverInitialBalance = testWallet2.getBalance();
        BigDecimal transferAmount = new BigDecimal("100.00");

        //Act
        TransferResponse response = userService.processTransfer(transferRequest);


        assertNotNull(response, "Response should not be null");
        assertEquals("251911223366", response.getSenderPhone());
        assertEquals("251911223344", response.getReceiverPhone());
        assertEquals(transferAmount, response.getAmount());
        assertEquals(senderInitialBalance.subtract(transferAmount), response.getNewBalance());
        //assert
        assertEquals(senderInitialBalance.subtract(transferAmount), testWallet.getBalance());
        assertEquals(receiverInitialBalance.add(transferAmount), testWallet2.getBalance());

        //verify
        verify(walletRepository, times(1)).findByUserPhoneForUpdate(anyString());
        verify(walletRepository).findByUserPhoneNumber(anyString());
        verify(pinUtil).verifyPin("1234", "hashed-pin", "testSalt");
        verify(walletRepository, times(2)).save(any(Wallet.class));

        System.out.println("✅ Transfer test passed!");


         }
    @Test
    @DisplayName("Should throw an error when sender has insufficient balance")
    void testProcessTransfer_insufficientBalance(){

        when(walletRepository.findByUserPhoneForUpdate("251911223366")).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByUserPhoneNumber("251911223344")).thenReturn(Optional.of(testWallet2));
        when(pinUtil.verifyPin("1234", "hashed-pin", "testSalt")).thenReturn(true);
        // Set transfer amount greater than sender's balance
        BigDecimal senderInitialBalance = testWallet.getBalance();
        BigDecimal receiverInitialBalance = testWallet2.getBalance();

        /* expect an exception
            act
         */
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.processTransfer(transferRequest));

        assertEquals("Insufficient balance or daily limit exceeded", exception.getMessage());
        //balance should not change
        assertEquals(senderInitialBalance, testWallet.getBalance());  // Still 1000
        assertEquals(receiverInitialBalance, testWallet2.getBalance());

        // Verify interactions - find methods were called, but save was NOT called
        verify(walletRepository).findByUserPhoneForUpdate("251911223366");
        verify(walletRepository).findByUserPhoneNumber("251911223344");
        verify(pinUtil).verifyPin("1234", "hashed-pin", "testSalt");
        verify(walletRepository, never()).save(any(Wallet.class));  // ❌ No save!

        System.out.println("✅ Insufficient balance test passed!");


    }


}