package com.gpsit.sistema.estoque.web;

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
@DisplayName("EstoqueController — cobertura adicional")
class EstoqueControllerCoberturaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private Produto criarProdutoNoBanco(String nome, String codigo, Categoria cat, int qtd, int min) {
        Produto p = new Produto(nome, codigo, cat,
                UnidadeMedida.LITRO, new BigDecimal("5.99"), qtd, min);
        return repository.save(p);
    }

    @Test
    @DisplayName("GET /produtos?categoria=BEBIDAS — filtra por categoria específica")
    void listar_flitroPorCategoria() throws Exception {
        criarProdutoNoBanco("Suco", "CAT-001", Categoria.BEBIDAS, 20, 5);
        criarProdutoNoBanco("Detergente", "CAT-002", Categoria.LIMPEZA, 15, 3);

        mockMvc.perform(get("/produtos").param("categoria", "BEBIDAS"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("filtroAtivo"));
    }

    @Test
    @DisplayName("GET /produtos?categoria=INVALIDA — categoria inválida retorna todos")
    void listar_categoriaInvalida_retornaTodos() throws Exception {
        criarProdutoNoBanco("ProdX", "INV-001", Categoria.OUTROS, 10, 2);

        mockMvc.perform(get("/produtos").param("categoria", "CATEGORIA_INVALIDA"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/listar"));
    }

    @Test
    @DisplayName("GET /produtos?categoria= — categoria em branco retorna todos")
    void listar_categoriaEmBranco_retornaTodos() throws Exception {
        mockMvc.perform(get("/produtos").param("categoria", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/listar"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/editar — atualiza produto com sucesso")
    void atualizarProduto_sucesso() throws Exception {
        Produto p = criarProdutoNoBanco("ProdEdit", "EDT-001", Categoria.OUTROS, 10, 2);

        mockMvc.perform(post("/produtos/" + p.getId() + "/editar")
                .param("nome", "ProdEditado")
                .param("codigoBarras", "EDT-001")
                .param("categoria", "OUTROS")
                .param("unidade", "UNIDADE")
                .param("preco", "10.00")
                .param("quantidade", "10")
                .param("estoqueMinimo", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/editar — binding errors retorna formulário")
    void atualizarProduto_bindingErrors_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("ProdEdit2", "EDT-002", Categoria.OUTROS, 10, 2);

        mockMvc.perform(post("/produtos/" + p.getId() + "/editar")
                .param("nome", "")
                .param("codigoBarras", "")
                .param("preco", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/formulario"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("GET /produtos/{id}/editar — ID inexistente redireciona com erro")
    void editar_idInexistente_redireciona() throws Exception {
        mockMvc.perform(get("/produtos/99999/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/remover — ID inexistente redireciona com erro")
    void remover_idInexistente_redireciona() throws Exception {
        mockMvc.perform(post("/produtos/99999/remover"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /produtos/{id}/saida — exibe formulário de saída")
    void saida_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("ProdSaida", "SAI-001", Categoria.CARNES, 50, 5);

        mockMvc.perform(get("/produtos/" + p.getId() + "/saida"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/movimentacao"))
                .andExpect(model().attribute("tipoMovimentacao", "saida"));
    }

    @Test
    @DisplayName("GET /produtos/{id}/entrada — ID inexistente redireciona com erro")
    void entrada_idInexistente_redireciona() throws Exception {
        mockMvc.perform(get("/produtos/99999/entrada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /produtos/{id}/saida — ID inexistente redireciona com erro")
    void saida_idInexistente_redireciona() throws Exception {
        mockMvc.perform(get("/produtos/99999/saida"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/saida — estoque insuficiente redireciona com erro")
    void saida_estoqueInsuficiente_redirecionaComErro() throws Exception {
        Produto p = criarProdutoNoBanco("ProdPoucoEstq", "SAI-002", Categoria.OUTROS, 2, 1);

        mockMvc.perform(post("/produtos/" + p.getId() + "/saida")
                .param("quantidade", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/entrada — binding error retorna formulário")
    void entrada_bindingError_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("ProdBindErr", "BND-001", Categoria.OUTROS, 10, 2);

        mockMvc.perform(post("/produtos/" + p.getId() + "/entrada")
                .param("quantidade", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /produtos/{id}/saida — binding error retorna formulário")
    void saida_bindingError_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("ProdBindErr2", "BND-002", Categoria.OUTROS, 10, 2);

        mockMvc.perform(post("/produtos/" + p.getId() + "/saida")
                .param("quantidade", "0"))
                .andExpect(status().isOk());
    }
}
