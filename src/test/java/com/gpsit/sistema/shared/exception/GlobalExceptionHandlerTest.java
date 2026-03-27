package com.gpsit.sistema.shared.exception;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.Produto;
import com.gpsit.sistema.estoque.domain.UnidadeMedida;
import com.gpsit.sistema.estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void limpar() {
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("RecursoNaoEncontradoException retorna página de erro 404")
    void recursoNaoEncontrado_retorna404() throws Exception {
        mockMvc.perform(get("/produtos/99999/editar"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("RegraNegocioException retorna página de erro 400 ao criar produto duplicado")
    void regraNegocioViolada_retornaFormularioComErro() throws Exception {
        Produto p = new Produto("Produto1", "DUPE-001", Categoria.OUTROS,
                UnidadeMedida.UNIDADE, BigDecimal.ONE, 10, 2);
        produtoRepository.save(p);

        mockMvc.perform(post("/produtos/novo")
                        .param("nome", "Produto2")
                        .param("codigoBarras", "DUPE-001")
                        .param("categoria", "OUTROS")
                        .param("unidade", "UNIDADE")
                        .param("preco", "5.00")
                        .param("quantidade", "10")
                        .param("estoqueMinimo", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("erroNegocio"));
    }

    @Test
    @DisplayName("EstoqueInsuficiente resulta em redirect com mensagem de erro")
    void estoqueInsuficiente_retornaRedirectComErro() throws Exception {
        Produto p = new Produto("ProdutoX", "EST-001", Categoria.BEBIDAS,
                UnidadeMedida.LITRO, new BigDecimal("3.00"), 2, 1);
        p = produtoRepository.save(p);

        mockMvc.perform(post("/produtos/" + p.getId() + "/saida")
                        .param("quantidade", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("DadosInvalidosException resulta em redirect com mensagem de erro")
    void dadosInvalidos_retornaRedirectComErro() throws Exception {
        Produto p = new Produto("ProdutoY", "DAD-001", Categoria.OUTROS,
                UnidadeMedida.UNIDADE, BigDecimal.ONE, 10, 2);
        p = produtoRepository.save(p);

        mockMvc.perform(post("/produtos/" + p.getId() + "/entrada")
                        .param("quantidade", "0"))
                .andExpect(status().isOk());
    }
}
