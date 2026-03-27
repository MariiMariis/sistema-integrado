package com.gpsit.sistema.leads.domain;

public enum StatusLead {

    NOVO("Novo"),
    CONTATADO("Contatado"),
    QUALIFICADO("Qualificado"),
    CONVERTIDO("Convertido"),
    PERDIDO("Perdido");

    private final String descricao;

    StatusLead(String descricao) {
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
