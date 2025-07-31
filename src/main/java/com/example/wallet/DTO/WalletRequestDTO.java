package com.example.wallet.DTO;

import jakarta.validation.constraints.NotBlank;

public record WalletRequestDTO(@NotBlank String owner) {
}
