package com.example.wallet.service;

import com.example.wallet.DTO.WalletOperationRequestDTO;
import com.example.wallet.DTO.WalletRequestDTO;
import com.example.wallet.DTO.WalletResponseDTO;
import com.example.wallet.DTO.WalletUpdateRequestDTO;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.model.*;
import com.example.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletResponseDTO createWallet(WalletRequestDTO dto) {
        Wallet wallet = new Wallet();
        wallet.setOwner(dto.owner());
        wallet.setBalance(BigDecimal.ZERO);
        Wallet saved = walletRepository.save(wallet);
        return new WalletResponseDTO(saved.getId(), saved.getOwner(), saved.getBalance());
    }
    public WalletResponseDTO getWalletById(UUID id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        return new WalletResponseDTO(wallet.getId(), wallet.getOwner(), wallet.getBalance());
    }

    public Wallet updateBalance(UUID id, BigDecimal newBalance) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    public WalletResponseDTO changeBalance(UUID id, WalletOperationRequestDTO dto) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        BigDecimal newBalance;
        if (dto.getOperationType() == OperationType.DEPOSIT) {
            newBalance = wallet.getBalance().add(dto.getAmount());
        }else {
            if (wallet.getBalance().compareTo(dto.getAmount()) < 0) {
                throw new RuntimeException("Insufficient funds");
            }
            newBalance = wallet.getBalance().subtract(dto.getAmount());
        }
        wallet.setBalance(newBalance);
        Wallet updated = walletRepository.save(wallet);
        return new WalletResponseDTO(updated.getId(), updated.getOwner(), updated.getBalance());
    }

    @Transactional
    public WalletResponseDTO updateWallet(UUID id, WalletUpdateRequestDTO updateRequest) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet with id " + id + " not found"));
        wallet.setOwner(updateRequest.getOwner());
        Wallet updated = walletRepository.save(wallet);
        return new WalletResponseDTO(updated.getId(), updated.getOwner(), updated.getBalance());
    }
    @Transactional
    public void deleteWallet(UUID id) {
        if (!walletRepository.existsById(id)) {
            throw new RuntimeException("Wallet not found");
        }
        walletRepository.deleteById(id);
    }

}
