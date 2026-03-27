package com.gpsit.sistema.estoque.service;

import java.math.BigDecimal;

public record ProdutoAtualizacaoDTO(
        String novoNome,
        BigDecimal novoPreco,
        Integer novoEstoqueMinimo
) {}
