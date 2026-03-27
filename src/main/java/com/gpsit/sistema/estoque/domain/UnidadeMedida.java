package com.gpsit.sistema.estoque.domain;

public enum UnidadeMedida {

    QUILOGRAMA("kg"),
    GRAMA("g"),
    LITRO("L"),
    MILILITRO("mL"),
    UNIDADE("un"),
    CAIXA("cx"),
    PACOTE("pct");

    private final String simbolo;

    UnidadeMedida(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public String toString() {
        return simbolo;
    }
}
