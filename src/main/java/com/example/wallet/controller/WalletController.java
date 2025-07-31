package com.example.wallet.controller;

import com.example.wallet.DTO.WalletOperationRequestDTO;
import com.example.wallet.DTO.WalletRequestDTO;
import com.example.wallet.DTO.WalletResponseDTO;
import com.example.wallet.DTO.WalletUpdateRequestDTO;
import com.example.wallet.model.*;
import com.example.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponseDTO createWallet(@Valid @RequestBody WalletRequestDTO dto) {
        try {
            return walletService.createWallet(dto);
        } catch (Exception ex) {
            log.error("Ошибка при создании кошелька", ex);
            throw ex;
        }
    }


    @PutMapping("/{id}/balance")
    public ResponseEntity<Wallet> updateBalance(@PathVariable UUID id, @RequestParam BigDecimal balance) {
        return ResponseEntity.ok(walletService.updateBalance(id, balance));
    }
    @PatchMapping("/{id}")
    public WalletResponseDTO updateBalance(
            @PathVariable UUID id,
            @Valid @RequestBody WalletOperationRequestDTO dto
    ) {
        return walletService.changeBalance(id, dto);
    }
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponseDTO> getWalletById(@PathVariable UUID id) {
        WalletResponseDTO wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }
    @PutMapping("/{id}")
    public ResponseEntity<WalletResponseDTO> updateWallet(
            @PathVariable UUID id,
            @RequestBody WalletUpdateRequestDTO updateRequest
    ) {
        WalletResponseDTO updatedWallet = walletService.updateWallet(id, updateRequest);
        return ResponseEntity.ok(updatedWallet);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}
