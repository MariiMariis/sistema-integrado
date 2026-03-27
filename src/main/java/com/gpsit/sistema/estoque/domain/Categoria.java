package com.gpsit.sistema.estoque.domain;

public enum Categoria {

    HORTIFRUTI("Hortifruti"),
    LATICINIOS("Laticínios"),
    CARNES("Carnes e Aves"),
    PADARIA("Padaria"),
    BEBIDAS("Bebidas"),
    LIMPEZA("Limpeza"),
    HIGIENE("Higiene Pessoal"),
    OUTROS("Outros");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
