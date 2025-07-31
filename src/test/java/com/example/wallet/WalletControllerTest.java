package com.example.wallet;
import com.example.wallet.DTO.*;
import com.example.wallet.controller.WalletController;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.model.OperationType;
import com.example.wallet.model.Wallet;
import com.example.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private WalletService walletService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    @Test
    @DisplayName("POST /api/wallets - Успешное создание кошелька")
    void createWallet_success() throws Exception {
        UUID id = UUID.randomUUID();
        WalletResponseDTO responseDTO = new WalletResponseDTO(id, "TestUser", BigDecimal.ZERO);
        when(walletService.createWallet(any(WalletRequestDTO.class))).thenReturn(responseDTO);
        String jsonRequest = """
            {
                "owner": "TestUser"
            }
            """;

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.owner").value("TestUser"))
                .andExpect(jsonPath("$.balance").value(0));
    }
    @Test
    @DisplayName("GET /api/wallets/{id} - Получение кошелька по ID")
    void getWalletById_success() throws Exception {
        UUID id = UUID.randomUUID();
        WalletResponseDTO responseDTO = new WalletResponseDTO(id, "TestUser", BigDecimal.valueOf(100));
        when(walletService.getWalletById(id)).thenReturn(responseDTO);
        mockMvc.perform(get("/api/wallets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.owner").value("TestUser"))
                .andExpect(jsonPath("$.balance").value(100));
    }
    @Test
    @DisplayName("DELETE /api/wallets/{id} - Успешное удаление")
    void deleteWallet_success() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.doNothing().when(walletService).deleteWallet(id);

        mockMvc.perform(delete("/api/wallets/{id}", id))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("PUT /api/wallets/{id}/balance - Успешное обновление баланса")
    void updateBalance_success()throws Exception {
        UUID id = UUID.randomUUID();
        Wallet updateWallet = new Wallet(id, BigDecimal.valueOf(500));
        updateWallet.setOwner("UserA");
        when(walletService.updateBalance(eq(id), eq(BigDecimal.valueOf(500))))
                .thenReturn(updateWallet);
        mockMvc.perform(put("/api/wallets/{id}/balance", id)
                        .param("balance", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(500));

    }
    @Test
    @DisplayName("PATCH /api/wallets/{id} - Успешный депозит")
    void changeBalance_deposit_success() throws Exception {
        UUID id = UUID.randomUUID();
        WalletOperationRequestDTO requestDTO = new WalletOperationRequestDTO();
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(BigDecimal.valueOf(100));

        WalletResponseDTO responseDTO = new WalletResponseDTO(id, "UserB", BigDecimal.valueOf(200));

        when(walletService.changeBalance(eq(id), any(WalletOperationRequestDTO.class)))
                .thenReturn(responseDTO);

        String jsonRequest = """
        {
            "operationType": "DEPOSIT",
            "amount": 100
        }
        """;

        mockMvc.perform(patch("/api/wallets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.owner").value("UserB"))
                .andExpect(jsonPath("$.balance").value(200));
    }
    @Test
    @DisplayName("PUT /api/wallets/{id} - Успешное обновление владельца")
    void updateWallet_success() throws Exception {
        UUID id = UUID.randomUUID();
        WalletUpdateRequestDTO updateRequest = new WalletUpdateRequestDTO();
        updateRequest.setOwner("NewOwner");

        WalletResponseDTO updatedWallet = new WalletResponseDTO(id, "NewOwner", BigDecimal.valueOf(300));

        when(walletService.updateWallet(eq(id), any(WalletUpdateRequestDTO.class)))
                .thenReturn(updatedWallet);

        String jsonRequest = """
        {
            "owner": "NewOwner"
        }
        """;

        mockMvc.perform(put("/api/wallets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.owner").value("NewOwner"))
                .andExpect(jsonPath("$.balance").value(300));
    }
    @Test
    void getWalletById_shouldReturn404_whenWalletNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(walletService.getWalletById(id)).thenThrow(new WalletNotFoundException("Wallet with id " + id + " not found"));

        mockMvc.perform(get("/api/wallets/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wallet with id " + id + " not found"))
                .andExpect(jsonPath("$.status").value(404));
    }
    @Test
    void changeBalance_shouldReturn400_whenInsufficientFunds() throws Exception {
        UUID id = UUID.randomUUID();
        WalletOperationRequestDTO dto = new WalletOperationRequestDTO();
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(BigDecimal.valueOf(100));

        when(walletService.changeBalance(eq(id), any())).thenThrow(new RuntimeException("Insufficient funds"));

        mockMvc.perform(patch("/api/wallets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }
    @Test
    void deleteWallet_shouldReturn400_whenWalletNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new RuntimeException("Wallet not found")).when(walletService).deleteWallet(id);

        mockMvc.perform(delete("/api/wallets/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Wallet not found"));
    }
    @Test
    void changeBalance_shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
        UUID id = UUID.randomUUID();
        WalletOperationRequestDTO dto = new WalletOperationRequestDTO();
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(BigDecimal.valueOf(10));

        when(walletService.changeBalance(eq(id), any())).thenThrow(new NullPointerException("Something went wrong"));

        mockMvc.perform(patch("/api/wallets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Something went wrong"));
    }
    @Test
    void createWallet_shouldReturn400_whenOwnerIsBlank() throws Exception {
        WalletRequestDTO dto = new WalletRequestDTO("");

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

}



