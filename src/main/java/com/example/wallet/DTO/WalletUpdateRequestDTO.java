package com.example.wallet.DTO;

import lombok.Data;

@Data
public class WalletUpdateRequestDTO {
    private String owner;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
