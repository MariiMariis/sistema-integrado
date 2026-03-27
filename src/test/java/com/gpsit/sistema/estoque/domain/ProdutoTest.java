package com.gpsit.sistema.estoque.domain;

import com.gpsit.sistema.shared.exception.DadosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Produto")
class ProdutoTest {

    static Produto produtoValido() {
        return new Produto(
                "Leite Integral",
                "7891234567890",
                Categoria.LATICINIOS,
                UnidadeMedida.LITRO,
                new BigDecimal("4.99"),
                100,
                10);
    }

    @Nested
    @DisplayName("Validação do nome")
    class ValidacaoNome {

        @Test
        @DisplayName("Aceita nome com 2 caracteres — limite mínimo válido")
        void nomeComDoisCaracteres_valido() {
            assertDoesNotThrow(() -> new Produto("AB", "111",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Aceita nome com 100 caracteres — limite máximo válido")
        void nomeComCemCaracteres_valido() {
            String nomeMaximo = "A".repeat(100);
            assertDoesNotThrow(() -> new Produto(nomeMaximo, "222",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Rejeita nome nulo")
        void nomeNulo_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto(null, "333",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @ParameterizedTest(name = "Rejeita nome: \"{0}\"")
        @ValueSource(strings = {"", "  ", "\t"})
        @DisplayName("Rejeita nome vazio ou apenas espaços")
        void nomeVazioOuEspacos_lancaExcecao(String nomeInvalido) {
            assertThrows(DadosInvalidosException.class, () -> new Produto(nomeInvalido, "444",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Rejeita nome com 1 caractere — abaixo do limite mínimo")
        void nomeComUmCaractere_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto("X", "555",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Rejeita nome com 101 caracteres — acima do limite máximo")
        void nomeComCentoEUmCaracteres_lancaExcecao() {
            String nomeExcedente = "A".repeat(101);
            assertThrows(DadosInvalidosException.class, () -> new Produto(nomeExcedente, "666",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }
    }

    @Nested
    @DisplayName("Validação do preço")
    class ValidacaoPreco {

        @Test
        @DisplayName("Aceita preço zero — limite inferior válido")
        void precoZero_valido() {
            assertDoesNotThrow(() -> new Produto("Brinde", "777",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ZERO, 0, 0));
        }

        @Test
        @DisplayName("Aceita preço positivo")
        void precoPositivo_valido() {
            assertDoesNotThrow(() -> new Produto("Produto X", "888",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    new BigDecimal("0.01"), 1, 0));
        }

        @Test
        @DisplayName("Rejeita preço negativo")
        void precoNegativo_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto("Produto Y", "999",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    new BigDecimal("-0.01"), 1, 0));
        }

        @Test
        @DisplayName("Rejeita preço nulo")
        void precoNulo_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto("Produto Z", "000",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    null, 1, 0));
        }
    }

    @Nested
    @DisplayName("Validação de quantidades")
    class ValidacaoQuantidades {

        @Test
        @DisplayName("Aceita quantidade zero")
        void quantidadeZero_valido() {
            assertDoesNotThrow(() -> criarComQuantidade(0));
        }

        @Test
        @DisplayName("Rejeita quantidade negativa")
        void quantidadeNegativa_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> criarComQuantidade(-1));
        }

        @Test
        @DisplayName("Aceita estoque mínimo zero")
        void estoqueMinimZero_valido() {
            assertDoesNotThrow(() -> criarComEstoqueMinimo(0));
        }

        @Test
        @DisplayName("Rejeita estoque mínimo negativo")
        void estoqueMinimNegativo_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> criarComEstoqueMinimo(-1));
        }

        private Produto criarComQuantidade(int quantidade) {
            return new Produto("Produto W", "101",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, quantidade, 0);
        }

        private Produto criarComEstoqueMinimo(int minimo) {
            return new Produto("Produto V", "102",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 0, minimo);
        }
    }

    @Nested
    @DisplayName("Métodos de cópia imutável")
    class CopiaImutavel {

        @Test
        @DisplayName("comQuantidade retorna novo objeto sem alterar o original")
        void comQuantidade_retornaNovaInstancia() {
            Produto original = produtoValido();
            original.setId(1L);
            Produto atualizado = original.comQuantidade(200);

            assertNotSame(original, atualizado);
            assertEquals(100, original.getQuantidade());
            assertEquals(200, atualizado.getQuantidade());
            assertEquals(original.getId(), atualizado.getId());
        }

        @Test
        @DisplayName("comPreco retorna novo objeto sem alterar o original")
        void comPreco_retornaNovaInstancia() {
            Produto original = produtoValido();
            original.setId(1L);
            BigDecimal novoPreco = new BigDecimal("9.99");
            Produto atualizado = original.comPreco(novoPreco);

            assertNotSame(original, atualizado);
            assertEquals(new BigDecimal("4.99"), original.getPreco());
            assertEquals(novoPreco, atualizado.getPreco());
        }

        @Test
        @DisplayName("comEstoqueMinimo retorna novo objeto sem alterar o original")
        void comEstoqueMinimo_retornaNovaInstancia() {
            Produto original = produtoValido();
            original.setId(1L);
            Produto atualizado = original.comEstoqueMinimo(50);

            assertNotSame(original, atualizado);
            assertEquals(10, original.getEstoqueMinimo());
            assertEquals(50, atualizado.getEstoqueMinimo());
        }
    }

    @Nested
    @DisplayName("Estoque baixo")
    class EstoqueBaixo {

        @Test
        @DisplayName("Retorna true quando quantidade < estoque mínimo")
        void quantidadeAbaixoDoMinimo_true() {
            Produto produto = new Produto("ProdA", "103",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 5, 10);
            assertTrue(produto.estaBaixoDoEstoqueMinimo());
        }

        @Test
        @DisplayName("Retorna false quando quantidade == estoque mínimo — limite")
        void quantidadeIgualAoMinimo_false() {
            Produto produto = new Produto("ProdB", "104",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 10, 10);
            assertFalse(produto.estaBaixoDoEstoqueMinimo());
        }

        @Test
        @DisplayName("Retorna false quando quantidade > estoque mínimo")
        void quantidadeAcimaDoMinimo_false() {
            Produto produto = new Produto("ProdC", "105",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 20, 10);
            assertFalse(produto.estaBaixoDoEstoqueMinimo());
        }
    }

    @Nested
    @DisplayName("Validação do código de barras")
    class ValidacaoCodigoBarras {

        @Test
        @DisplayName("Rejeita código de barras nulo")
        void codigoBarrasNulo_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto("Teste", null,
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Rejeita código de barras vazio")
        void codigoBarrasVazio_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> new Produto("Teste", "",
                    Categoria.OUTROS, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }
    }

    @Nested
    @DisplayName("Validação de campos obrigatórios")
    class CamposObrigatorios {

        @Test
        @DisplayName("Rejeita categoria nula")
        void categoriaNula_lancaExcecao() {
            assertThrows(NullPointerException.class, () -> new Produto("Teste", "123",
                    null, UnidadeMedida.UNIDADE,
                    BigDecimal.ONE, 1, 0));
        }

        @Test
        @DisplayName("Rejeita unidade de medida nula")
        void unidadeNula_lancaExcecao() {
            assertThrows(NullPointerException.class, () -> new Produto("Teste", "123",
                    Categoria.OUTROS, null,
                    BigDecimal.ONE, 1, 0));
        }
    }

    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("Produtos com mesmo ID são iguais")
        void mesmoId_saoIguais() {
            Produto p1 = produtoValido();
            p1.setId(1L);
            Produto p2 = new Produto("Outro", "999",
                    Categoria.BEBIDAS, UnidadeMedida.LITRO,
                    BigDecimal.TEN, 5, 1);
            p2.setId(1L);
            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        @DisplayName("Produtos com IDs diferentes não são iguais")
        void idsDiferentes_naoSaoIguais() {
            Produto p1 = produtoValido();
            p1.setId(1L);
            Produto p2 = produtoValido();
            p2.setId(2L);
            assertNotEquals(p1, p2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString contém informações relevantes")
        void toStringContemInformacoes() {
            Produto p = produtoValido();
            p.setId(1L);
            String resultado = p.toString();
            assertTrue(resultado.contains("Leite Integral"));
            assertTrue(resultado.contains("Latic"));
        }
    }
}
