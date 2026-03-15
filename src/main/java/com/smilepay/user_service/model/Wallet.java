package com.smilepay.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "wallets")
@Data
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50, unique = true)
    private String walletNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance= new BigDecimal("1000");

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("50000");

    private LocalDateTime lastResetDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyUsed = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate(){
        this.walletNumber= "SML" + System.currentTimeMillis() % 1000000; // Generate a unique wallet number
        this.lastResetDate = LocalDateTime.now();
    }

    public boolean canDebit(BigDecimal amount){
        if (balance.compareTo(amount)<0) return false;
        if(dailyUsed.add(amount).compareTo(dailyLimit)>0) return false;

        return true;
    }

    public void debit(BigDecimal amount){
        if(!canDebit(amount)) throw new IllegalStateException("Insufficient balance or daily limit exceeded");

        this.balance = this.balance.subtract(amount);
        this.dailyUsed = this.dailyUsed.add(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
