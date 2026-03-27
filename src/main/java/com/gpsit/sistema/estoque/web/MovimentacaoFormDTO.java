package com.gpsit.sistema.estoque.web;

import jakarta.validation.constraints.Min;

public class MovimentacaoFormDTO {

    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private int quantidade;

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
