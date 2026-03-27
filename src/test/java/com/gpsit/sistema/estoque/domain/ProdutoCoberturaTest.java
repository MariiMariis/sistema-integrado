package com.gpsit.sistema.estoque.domain;

import com.gpsit.sistema.shared.exception.DadosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Produto — cobertura adicional")
class ProdutoCoberturaTest {

    private Produto criarProdutoBase() {
        return new Produto("Teste", "COB-001", Categoria.OUTROS,
                UnidadeMedida.UNIDADE, BigDecimal.ONE, 10, 2);
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setNome atualiza nome válido")
        void setNomeValido() {
            Produto p = criarProdutoBase();
            p.setNome("Novo Nome");
            assertEquals("Novo Nome", p.getNome());
        }

        @Test
        @DisplayName("setNome rejeita nome vazio")
        void setNomeInvalido() {
            Produto p = criarProdutoBase();
            assertThrows(DadosInvalidosException.class, () -> p.setNome(""));
        }

        @Test
        @DisplayName("setCodigoBarras atualiza código válido")
        void setCodigoBarrasValido() {
            Produto p = criarProdutoBase();
            p.setCodigoBarras("NOVO-COD");
            assertEquals("NOVO-COD", p.getCodigoBarras());
        }

        @Test
        @DisplayName("setCodigoBarras rejeita código nulo")
        void setCodigoBarrasNulo() {
            Produto p = criarProdutoBase();
            assertThrows(DadosInvalidosException.class, () -> p.setCodigoBarras(null));
        }

        @Test
        @DisplayName("setCategoria atualiza categoria")
        void setCategoriaValida() {
            Produto p = criarProdutoBase();
            p.setCategoria(Categoria.BEBIDAS);
            assertEquals(Categoria.BEBIDAS, p.getCategoria());
        }

        @Test
        @DisplayName("setCategoria rejeita nula")
        void setCategoriaNula() {
            Produto p = criarProdutoBase();
            assertThrows(NullPointerException.class, () -> p.setCategoria(null));
        }

        @Test
        @DisplayName("setUnidade atualiza unidade")
        void setUnidadeValida() {
            Produto p = criarProdutoBase();
            p.setUnidade(UnidadeMedida.QUILOGRAMA);
            assertEquals(UnidadeMedida.QUILOGRAMA, p.getUnidade());
        }

        @Test
        @DisplayName("setUnidade rejeita nula")
        void setUnidadeNula() {
            Produto p = criarProdutoBase();
            assertThrows(NullPointerException.class, () -> p.setUnidade(null));
        }

        @Test
        @DisplayName("setPreco atualiza preço válido")
        void setPrecoValido() {
            Produto p = criarProdutoBase();
            p.setPreco(new BigDecimal("99.99"));
            assertEquals(new BigDecimal("99.99"), p.getPreco());
        }

        @Test
        @DisplayName("setPreco rejeita preço negativo")
        void setPrecoNegativo() {
            Produto p = criarProdutoBase();
            assertThrows(DadosInvalidosException.class, () -> p.setPreco(new BigDecimal("-1")));
        }

        @Test
        @DisplayName("setQuantidade atualiza quantidade válida")
        void setQuantidadeValida() {
            Produto p = criarProdutoBase();
            p.setQuantidade(50);
            assertEquals(50, p.getQuantidade());
        }

        @Test
        @DisplayName("setQuantidade rejeita quantidade negativa")
        void setQuantidadeNegativa() {
            Produto p = criarProdutoBase();
            assertThrows(DadosInvalidosException.class, () -> p.setQuantidade(-1));
        }

        @Test
        @DisplayName("setEstoqueMinimo atualiza estoque mínimo válido")
        void setEstoqueMinValido() {
            Produto p = criarProdutoBase();
            p.setEstoqueMinimo(20);
            assertEquals(20, p.getEstoqueMinimo());
        }

        @Test
        @DisplayName("setEstoqueMinimo rejeita valor negativo")
        void setEstoqueMinNegativo() {
            Produto p = criarProdutoBase();
            assertThrows(DadosInvalidosException.class, () -> p.setEstoqueMinimo(-1));
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("Categoria tem descrição correta")
        void categoriaDescricao() {
            assertEquals("Bebidas", Categoria.BEBIDAS.getDescricao());
            assertEquals("Laticínios", Categoria.LATICINIOS.getDescricao());
            assertEquals("Hortifruti", Categoria.HORTIFRUTI.getDescricao());
            assertEquals("Carnes e Aves", Categoria.CARNES.getDescricao());
            assertEquals("Padaria", Categoria.PADARIA.getDescricao());
            assertEquals("Limpeza", Categoria.LIMPEZA.getDescricao());
            assertEquals("Higiene Pessoal", Categoria.HIGIENE.getDescricao());
            assertEquals("Outros", Categoria.OUTROS.getDescricao());
        }

        @Test
        @DisplayName("UnidadeMedida tem símbolo correto")
        void unidadeSimbolo() {
            assertEquals("kg", UnidadeMedida.QUILOGRAMA.getSimbolo());
            assertEquals("g", UnidadeMedida.GRAMA.getSimbolo());
            assertEquals("L", UnidadeMedida.LITRO.getSimbolo());
            assertEquals("mL", UnidadeMedida.MILILITRO.getSimbolo());
            assertEquals("un", UnidadeMedida.UNIDADE.getSimbolo());
            assertEquals("cx", UnidadeMedida.CAIXA.getSimbolo());
            assertEquals("pct", UnidadeMedida.PACOTE.getSimbolo());
        }
    }

    @Nested
    @DisplayName("equals — edge cases")
    class EqualsEdgeCases {

        @Test
        @DisplayName("Produto com id null não é igual a outro com id null")
        void idsNull_naoSaoIguais() {
            Produto p1 = criarProdutoBase();
            Produto p2 = criarProdutoBase();
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("Produto é igual a si mesmo")
        void mesmaReferencia_igualASiMesmo() {
            Produto p = criarProdutoBase();
            assertEquals(p, p);
        }

        @Test
        @DisplayName("Produto não é igual a null")
        void comparacaoComNull_naoEhIgual() {
            Produto p = criarProdutoBase();
            assertNotEquals(null, p);
        }

        @Test
        @DisplayName("Produto não é igual a outro tipo")
        void comparacaoComOutroTipo_naoEhIgual() {
            Produto p = criarProdutoBase();
            assertNotEquals("string", p);
        }
    }
}
