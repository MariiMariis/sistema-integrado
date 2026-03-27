package com.gpsit.sistema.estoque.service;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.Produto;
import com.gpsit.sistema.estoque.domain.UnidadeMedida;
import com.gpsit.sistema.estoque.repository.ProdutoRepository;
import com.gpsit.sistema.shared.exception.DadosInvalidosException;
import com.gpsit.sistema.shared.exception.RecursoNaoEncontradoException;
import com.gpsit.sistema.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("EstoqueService")
class EstoqueServiceTest {

    @Autowired
    private EstoqueService service;

    @Autowired
    private ProdutoRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private Produto criarProdutoNoServico(String nome, String codigo, int qtd, int min) {
        return service.criarProduto(new ProdutoCriacaoDTO(
                nome, codigo, Categoria.OUTROS, UnidadeMedida.UNIDADE,
                new BigDecimal("5.00"), qtd, min));
    }

    @Nested
    @DisplayName("criarProduto")
    class CriarProduto {

        @Test
        @DisplayName("Cria produto com dados válidos")
        void dadosValidos_criaProduto() {
            Produto produto = criarProdutoNoServico("Arroz", "EAN-A01", 50, 5);
            assertNotNull(produto.getId());
            assertEquals(1, service.listarTodos().size());
        }

        @Test
        @DisplayName("Lança exceção para código de barras duplicado")
        void codigoBarrasDuplicado_lancaExcecao() {
            criarProdutoNoServico("Feijão", "DUP-001", 30, 5);
            assertThrows(RegraNegocioException.class,
                    () -> criarProdutoNoServico("Outro", "DUP-001", 10, 2));
        }

        @Test
        @DisplayName("Lança exceção para dados de domínio inválidos")
        void dadosInvalidos_lancaExcecao() {
            assertThrows(DadosInvalidosException.class, () -> service.criarProduto(
                    new ProdutoCriacaoDTO("", "EAN-A02",
                            Categoria.OUTROS, UnidadeMedida.UNIDADE,
                            BigDecimal.ONE, 0, 0)));
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("Retorna produto para ID existente")
        void idExistente_retornaProduto() {
            Produto criado = criarProdutoNoServico("Macarrão", "EAN-B01", 20, 3);
            Produto resultado = service.buscarPorId(criado.getId());
            assertEquals("Macarrão", resultado.getNome());
        }

        @Test
        @DisplayName("Lança exceção para ID inexistente")
        void idInexistente_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.buscarPorId(999L));
        }
    }

    @Nested
    @DisplayName("atualizarProduto")
    class AtualizarProduto {

        @Test
        @DisplayName("Atualiza nome mantendo outros campos")
        void atualizarNome_mantemOutrosCampos() {
            Produto criado = criarProdutoNoServico("Açúcar", "EAN-C01", 100, 10);
            service.atualizarProduto(criado.getId(),
                    new ProdutoAtualizacaoDTO("Açúcar Refinado", null, null));
            Produto atualizado = service.buscarPorId(criado.getId());
            assertEquals("Açúcar Refinado", atualizado.getNome());
            assertEquals(100, atualizado.getQuantidade());
        }

        @Test
        @DisplayName("Atualiza preço e estoque mínimo")
        void atualizarPrecoEEstoqueMinimo() {
            Produto criado = criarProdutoNoServico("Sal", "EAN-C02", 50, 5);
            service.atualizarProduto(criado.getId(),
                    new ProdutoAtualizacaoDTO(null, new BigDecimal("2.49"), 8));
            Produto atualizado = service.buscarPorId(criado.getId());
            assertEquals(new BigDecimal("2.49"), atualizado.getPreco());
            assertEquals(8, atualizado.getEstoqueMinimo());
        }

        @Test
        @DisplayName("Lança exceção para ID inexistente")
        void idInexistente_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.atualizarProduto(999L,
                            new ProdutoAtualizacaoDTO(null, null, null)));
        }
    }

    @Nested
    @DisplayName("excluir")
    class Excluir {

        @Test
        @DisplayName("Remove produto existente")
        void produtoExistente_remove() {
            Produto criado = criarProdutoNoServico("Óleo", "EAN-D01", 30, 3);
            service.excluir(criado.getId());
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.buscarPorId(criado.getId()));
        }

        @Test
        @DisplayName("Lança exceção ao remover ID inexistente")
        void idInexistente_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.excluir(999L));
        }
    }

    @Nested
    @DisplayName("registrarEntrada")
    class RegistrarEntrada {

        @Test
        @DisplayName("Aumenta o estoque corretamente")
        void quantidadeValida_aumentaEstoque() {
            Produto criado = criarProdutoNoServico("Farinha", "EAN-E01", 10, 2);
            service.registrarEntrada(criado.getId(), 40);
            assertEquals(50, service.buscarPorId(criado.getId()).getQuantidade());
        }

        @Test
        @DisplayName("Lança exceção para quantidade zero")
        void quantidadeZero_lancaExcecao() {
            Produto criado = criarProdutoNoServico("Manteiga", "EAN-E02", 5, 1);
            assertThrows(DadosInvalidosException.class,
                    () -> service.registrarEntrada(criado.getId(), 0));
        }

        @Test
        @DisplayName("Lança exceção para quantidade negativa")
        void quantidadeNegativa_lancaExcecao() {
            Produto criado = criarProdutoNoServico("Creme", "EAN-E03", 5, 1);
            assertThrows(DadosInvalidosException.class,
                    () -> service.registrarEntrada(criado.getId(), -5));
        }
    }

    @Nested
    @DisplayName("registrarSaida")
    class RegistrarSaida {

        @Test
        @DisplayName("Debita estoque corretamente com estoque suficiente")
        void saidaComEstoqueSuficiente_debitaEstoque() {
            Produto criado = criarProdutoNoServico("Queijo", "EAN-F01", 20, 2);
            service.registrarSaida(criado.getId(), 8);
            assertEquals(12, service.buscarPorId(criado.getId()).getQuantidade());
        }

        @Test
        @DisplayName("Saída exata do estoque — limite")
        void saidaExata_zeroEstoque() {
            Produto criado = criarProdutoNoServico("Presunto", "EAN-F02", 10, 0);
            service.registrarSaida(criado.getId(), 10);
            assertEquals(0, service.buscarPorId(criado.getId()).getQuantidade());
        }

        @Test
        @DisplayName("Lança exceção para estoque insuficiente")
        void estoqueInsuficiente_lancaExcecao() {
            Produto criado = criarProdutoNoServico("Iogurte", "EAN-F03", 3, 1);
            assertThrows(RegraNegocioException.class,
                    () -> service.registrarSaida(criado.getId(), 5));
        }

        @Test
        @DisplayName("Lança exceção para produto inexistente")
        void produtoNaoExiste_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.registrarSaida(999L, 1));
        }

        @Test
        @DisplayName("Lança exceção para quantidade zero")
        void quantidadeZero_lancaExcecao() {
            Produto criado = criarProdutoNoServico("Mel", "EAN-F04", 5, 1);
            assertThrows(DadosInvalidosException.class,
                    () -> service.registrarSaida(criado.getId(), 0));
        }
    }

    @Nested
    @DisplayName("buscarComEstoqueBaixo")
    class BuscarComEstoqueBaixo {

        @Test
        @DisplayName("Retorna apenas produtos abaixo do mínimo")
        void retornaApenasAbaixoDoMinimo() {
            criarProdutoNoServico("ProdOK", "EAN-G01", 50, 10);
            criarProdutoNoServico("ProdBaixo", "EAN-G02", 3, 10);
            List<Produto> resultado = service.buscarComEstoqueBaixo();
            assertEquals(1, resultado.size());
            assertEquals("ProdBaixo", resultado.get(0).getNome());
        }

        @Test
        @DisplayName("Retorna lista vazia quando todos têm estoque adequado")
        void todosComEstoqueOk_retornaVazio() {
            criarProdutoNoServico("ProdA", "EAN-G03", 20, 5);
            criarProdutoNoServico("ProdB", "EAN-G04", 10, 10);
            assertTrue(service.buscarComEstoqueBaixo().isEmpty());
        }
    }

    @Nested
    @DisplayName("buscarPorCategoria")
    class BuscarPorCategoria {

        @Test
        @DisplayName("Filtra produtos pela categoria correta")
        void filtraPorCategoria() {
            service.criarProduto(new ProdutoCriacaoDTO("Refrigerante", "EAN-H01",
                    Categoria.BEBIDAS, UnidadeMedida.LITRO,
                    new BigDecimal("5.99"), 30, 5));
            service.criarProduto(new ProdutoCriacaoDTO("Detergente", "EAN-H02",
                    Categoria.LIMPEZA, UnidadeMedida.UNIDADE,
                    new BigDecimal("2.50"), 20, 4));

            List<Produto> bebidas = service.buscarPorCategoria(Categoria.BEBIDAS);
            assertEquals(1, bebidas.size());
            assertEquals("Refrigerante", bebidas.get(0).getNome());
        }
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("Retorna lista vazia quando não há produtos")
        void semProdutos_retornaVazio() {
            assertTrue(service.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Retorna todos os produtos cadastrados")
        void comProdutos_retornaTodos() {
            criarProdutoNoServico("Prod1", "EAN-I01", 10, 2);
            criarProdutoNoServico("Prod2", "EAN-I02", 20, 5);
            assertEquals(2, service.listarTodos().size());
        }
    }
}
