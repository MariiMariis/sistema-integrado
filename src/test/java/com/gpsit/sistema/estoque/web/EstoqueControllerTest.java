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
@DisplayName("EstoqueController")
class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private Produto criarProdutoNoBanco(String nome, String codigo) {
        Produto p = new Produto(nome, codigo, Categoria.BEBIDAS,
                UnidadeMedida.LITRO, new BigDecimal("5.99"), 30, 5);
        return repository.save(p);
    }

    @Test
    @DisplayName("GET /produtos — retorna página de listagem")
    void listar_retornaView() throws Exception {
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/listar"))
                .andExpect(model().attributeExists("produtos", "categorias"));
    }

    @Test
    @DisplayName("GET /produtos?estoqueBaixo=true — filtra produtos com estoque baixo")
    void listar_filtroEstoqueBaixo() throws Exception {
        Produto p = new Produto("ProdBaixo", "BAIXO-01", Categoria.OUTROS,
                UnidadeMedida.UNIDADE, BigDecimal.ONE, 2, 10);
        repository.save(p);

        mockMvc.perform(get("/produtos").param("estoqueBaixo", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("filtroAtivo"));
    }

    @Test
    @DisplayName("GET /produtos/novo — exibe formulário de novo produto")
    void novo_retornaFormulario() throws Exception {
        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/formulario"))
                .andExpect(model().attributeExists("produto", "categorias", "unidades"));
    }

    @Test
    @DisplayName("POST /produtos/novo — cria produto com sucesso")
    void criar_sucesso_redireciona() throws Exception {
        mockMvc.perform(post("/produtos/novo")
                        .param("nome", "Refrigerante")
                        .param("codigoBarras", "REF-001")
                        .param("categoria", "BEBIDAS")
                        .param("unidade", "LITRO")
                        .param("preco", "5.99")
                        .param("quantidade", "30")
                        .param("estoqueMinimo", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("POST /produtos/novo — validação falha retorna formulário")
    void criar_validacaoFalha_retornaFormulario() throws Exception {
        mockMvc.perform(post("/produtos/novo")
                        .param("nome", "")
                        .param("codigoBarras", "")
                        .param("preco", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/formulario"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("GET /produtos/{id}/editar — exibe formulário de edição")
    void editar_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("Suco", "SUC-001");

        mockMvc.perform(get("/produtos/" + p.getId() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/formulario"))
                .andExpect(model().attributeExists("produto", "produtoId"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/remover — remove produto com sucesso")
    void remover_sucesso() throws Exception {
        Produto p = criarProdutoNoBanco("Agua", "AGU-001");

        mockMvc.perform(post("/produtos/" + p.getId() + "/remover"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("GET /produtos/{id}/entrada — exibe formulário de entrada")
    void entrada_retornaFormulario() throws Exception {
        Produto p = criarProdutoNoBanco("Leite", "LET-001");

        mockMvc.perform(get("/produtos/" + p.getId() + "/entrada"))
                .andExpect(status().isOk())
                .andExpect(view().name("estoque/movimentacao"))
                .andExpect(model().attributeExists("produto", "movimentacao"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/entrada — registra entrada com sucesso")
    void registrarEntrada_sucesso() throws Exception {
        Produto p = criarProdutoNoBanco("Café", "CAF-001");

        mockMvc.perform(post("/produtos/" + p.getId() + "/entrada")
                        .param("quantidade", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("POST /produtos/{id}/saida — registra saída com sucesso")
    void registrarSaida_sucesso() throws Exception {
        Produto p = criarProdutoNoBanco("Chá", "CHA-001");

        mockMvc.perform(post("/produtos/" + p.getId() + "/saida")
                        .param("quantidade", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }
}
