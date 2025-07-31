package com.example.wallet;

import com.example.wallet.DTO.WalletRequestDTO;
import com.example.wallet.DTO.WalletResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletIntegrationTest {
    @Test
    void contextLoads() {
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createWallet_shouldReturnCreatedWallet() {
        WalletRequestDTO request = new WalletRequestDTO("IntegrationUser");
        ResponseEntity<WalletResponseDTO> response = restTemplate.postForEntity(
                "/api/wallets",
                request,
                WalletResponseDTO.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IntegrationUser", response.getBody().owner());
        assertEquals(BigDecimal.ZERO, response.getBody().balance());
        assertNotNull(response.getBody().id());
    }
    @Test
    void getWalletById_shouldReturnWallet() {
        // 1. Создание кошелька
        WalletRequestDTO request = new WalletRequestDTO("GetTestUser");
        ResponseEntity<WalletResponseDTO> postResponse = restTemplate.postForEntity(
                "/api/wallets", request, WalletResponseDTO.class);

        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        WalletResponseDTO created = postResponse.getBody();
        assertNotNull(created);
        UUID walletId = created.id();

        // 2. Получение кошелька по ID
        ResponseEntity<WalletResponseDTO> getResponse = restTemplate.getForEntity(
                "/api/wallets/" + walletId, WalletResponseDTO.class);

        // 3. Проверка
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("GetTestUser", getResponse.getBody().owner());
        assertEquals(0, getResponse.getBody().balance().compareTo(BigDecimal.ZERO));
        assertEquals(walletId, getResponse.getBody().id());
    }
}
