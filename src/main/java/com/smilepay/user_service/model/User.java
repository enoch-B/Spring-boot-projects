package com.smilepay.user_service.model;

import com.smilepay.user_service.enums.UserStatus;
import com.smilepay.user_service.enums.VerificationLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name= "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String fullName;

    private String email;

    @Column(nullable = false)
    private String pinHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status= UserStatus.PENDING;


   //timestamps
    @Column(name="created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name="updated_At")
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    @PrePersist
    protected void onCreate(){
        createdAt= LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

    //


}
