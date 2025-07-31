package com.example.wallet;

import com.example.wallet.DTO.WalletOperationRequestDTO;
import com.example.wallet.DTO.WalletRequestDTO;
import com.example.wallet.DTO.WalletResponseDTO;
import com.example.wallet.DTO.WalletUpdateRequestDTO;
import com.example.wallet.model.OperationType;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;
    @InjectMocks
    private WalletService walletService;
    private UUID walletId;
    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
    }

    @Test
    void createWallet_shouldReturnResponseDTO() {
        WalletRequestDTO dto = new WalletRequestDTO("TestUser");
        Wallet savedWallet = new Wallet(walletId, BigDecimal.ZERO);
        savedWallet.setOwner("TestUser");
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);
        WalletResponseDTO response = walletService.createWallet(dto);
        assertEquals(walletId, response.id());
        assertEquals("TestUser", response.owner());
        assertEquals(BigDecimal.ZERO, response.balance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
    @Test
    void getWalletById_shouldReturnWalletResponseDTO_whenWalletExists(){
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(50));
        wallet.setOwner ("ExistingUser");
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.of(wallet));
        WalletResponseDTO result = walletService.getWalletById(walletId);

        assertEquals(walletId, result.id());
        assertEquals("ExistingUser",result.owner());
        assertEquals(BigDecimal.valueOf(50), result.balance());
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void getWalletById_shouldThrowException_whenWalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.empty());
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.getWalletById(walletId)
        );

        assertEquals("Wallet with id " + walletId + " not found", exception.getMessage());
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void updateBalance_shouldUpdateAndReturnWallet() {
        Wallet existingWallet = new Wallet(walletId, BigDecimal.valueOf(20));
        existingWallet.setOwner("Deniz");
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BigDecimal newBalance = BigDecimal.valueOf(100);
        Wallet result = walletService.updateBalance(walletId, newBalance);
        assertEquals(newBalance, result.getBalance());
        assertEquals("Deniz", result.getOwner());
        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository, times(1)).save(existingWallet);
    }
    @Test
    void updateBalance_shouldThrowException_whenWalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.updateBalance(walletId, BigDecimal.valueOf(100))
        );
        assertEquals("Wallet with id " + walletId + " not found", exception.getMessage());
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void changeBalance_shouldDepositSuccessfully() {
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(50));
        wallet.setOwner("TestUser");
        WalletOperationRequestDTO dto = new WalletOperationRequestDTO();
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(BigDecimal.valueOf(30));
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WalletResponseDTO response = walletService.changeBalance(walletId, dto);
        assertEquals(BigDecimal.valueOf(80), response.balance());
        assertEquals("TestUser", response.owner());
        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void changeBalance_shouldWithdrawSuccessfully() {
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(100));
        wallet.setOwner("TestUser");
        WalletOperationRequestDTO dto = new WalletOperationRequestDTO();
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(BigDecimal.valueOf(40));
        when(walletRepository.findById(walletId)).thenReturn(java.util.Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WalletResponseDTO response = walletService.changeBalance(walletId, dto);
        assertEquals(BigDecimal.valueOf(60), response.balance());
    }
    @Test
    void changeBalance_shouldThrowException_whenInsufficientFunds() {
        UUID testId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Wallet wallet = new Wallet(testId, BigDecimal.valueOf(20)); // ✅ правильный ID
        wallet.setOwner("TestUser");

        WalletOperationRequestDTO dto = new WalletOperationRequestDTO();
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(BigDecimal.valueOf(50));

        when(walletRepository.findById(testId)).thenReturn(Optional.of(wallet)); // ✅ тот же ID

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.changeBalance(testId, dto) // ✅ тот же ID
        );

        assertEquals("Insufficient funds", exception.getMessage());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void updateWallet_shouldUpdateOwner() {
        Wallet existingWallet = new Wallet(walletId, BigDecimal.valueOf(100));
        existingWallet.setOwner("OldOwner");
        WalletUpdateRequestDTO updateDto = new WalletUpdateRequestDTO();
        updateDto.setOwner("NewOwner");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WalletResponseDTO result = walletService.updateWallet(walletId, updateDto);
        assertEquals("NewOwner", result.owner());
        assertEquals(BigDecimal.valueOf(100), result.balance());
        assertEquals(walletId, result.id());
        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository,times(1)).save(existingWallet);
    }
    @Test
    void deleteWallet_shouldThrowException_whenWalletNotFound() {
        when(walletRepository.existsById(walletId)).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, ()->walletService.deleteWallet(walletId));
        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository, never()).deleteById(walletId);

    }

    @Test
    void deleteWallet_shouldDeleteSuccessfully() {
        when(walletRepository.existsById(walletId)).thenReturn(true);
        walletService.deleteWallet(walletId);
        verify(walletRepository).existsById(walletId);
        verify(walletRepository).deleteById(walletId);
    }

}
