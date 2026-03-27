package com.gpsit.sistema.estoque.service;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.UnidadeMedida;

import java.math.BigDecimal;

public record ProdutoCriacaoDTO(
        String nome,
        String codigoBarras,
        Categoria categoria,
        UnidadeMedida unidade,
        BigDecimal preco,
        int quantidadeInicial,
        int estoqueMinimo
) {}
