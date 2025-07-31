package com.example.wallet.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponseDTO (UUID id, String owner, BigDecimal balance){
}
