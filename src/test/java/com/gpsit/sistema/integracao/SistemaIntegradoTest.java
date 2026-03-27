package com.gpsit.sistema.integracao;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.Produto;
import com.gpsit.sistema.estoque.domain.UnidadeMedida;
import com.gpsit.sistema.estoque.repository.ProdutoRepository;
import com.gpsit.sistema.estoque.service.EstoqueService;
import com.gpsit.sistema.estoque.service.ProdutoCriacaoDTO;
import com.gpsit.sistema.leads.domain.Lead;
import com.gpsit.sistema.leads.domain.StatusLead;
import com.gpsit.sistema.leads.repository.LeadRepository;
import com.gpsit.sistema.leads.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integração do Sistema")
class SistemaIntegradoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private LeadService leadService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LeadRepository leadRepository;

    @BeforeEach
    void limpar() {
        produtoRepository.deleteAll();
        leadRepository.deleteAll();
    }

    @Test
    @DisplayName("Spring context carrega com ambos os módulos")
    void contextCarrega() {
        assertNotNull(estoqueService);
        assertNotNull(leadService);
    }

    @Test
    @DisplayName("Dashboard exibe dados consolidados de ambos os módulos")
    void dashboard_exibeDadosConsolidados() throws Exception {

        estoqueService.criarProduto(new ProdutoCriacaoDTO(
                "Produto 1", "INT-001", Categoria.BEBIDAS,
                UnidadeMedida.LITRO, new BigDecimal("5.99"), 30, 5));
        estoqueService.criarProduto(new ProdutoCriacaoDTO(
                "Produto Baixo", "INT-002", Categoria.PADARIA,
                UnidadeMedida.UNIDADE, new BigDecimal("3.50"), 2, 10));

        leadService.salvar(new Lead("Lead Novo", "novo@test.com",
                "11999999999", StatusLead.NOVO, ""));
        leadService.salvar(new Lead("Lead Convertido", "conv@test.com",
                "11888888888", StatusLead.CONVERTIDO, ""));


        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("totalProdutos", 2))
                .andExpect(model().attribute("produtosEstoqueBaixo", 1))
                .andExpect(model().attribute("totalLeads", 2))
                .andExpect(model().attribute("leadsNovos", 1L))
                .andExpect(model().attribute("leadsConvertidos", 1L));
    }

    @Test
    @DisplayName("Raiz redireciona para dashboard")
    void raiz_redirecionaParaDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Fluxo completo: criar produto, criar lead, verificar ambos")
    void fluxoCompleto_criarEListar() {
        Produto produto = estoqueService.criarProduto(new ProdutoCriacaoDTO(
                "Arroz", "FL-001", Categoria.OUTROS,
                UnidadeMedida.QUILOGRAMA, new BigDecimal("7.50"), 50, 5));

        Lead lead = leadService.salvar(new Lead(
                "João", "joao@test.com", "11999999999", StatusLead.NOVO, ""));

        assertEquals(1, estoqueService.listarTodos().size());
        assertEquals(1, leadService.listarTodos().size());

        assertNotNull(produto.getId());
        assertNotNull(lead.getId());
        assertEquals("Arroz", estoqueService.buscarPorId(produto.getId()).getNome());
        assertEquals("João", leadService.buscarPorId(lead.getId()).getNome());
    }

    @Test
    @DisplayName("Operações em um módulo não afetam o outro")
    void modulos_independentes() {
        Produto produto = estoqueService.criarProduto(new ProdutoCriacaoDTO(
                "Feijão", "IND-001", Categoria.OUTROS,
                UnidadeMedida.QUILOGRAMA, new BigDecimal("8.00"), 30, 5));
        leadService.salvar(new Lead("Maria", "maria@test.com",
                "11777777777", StatusLead.QUALIFICADO, ""));

        estoqueService.excluir(produto.getId());

        assertEquals(0, estoqueService.listarTodos().size());
        assertEquals(1, leadService.listarTodos().size());
    }

    @Test
    @DisplayName("Dashboard funciona com dados vazios")
    void dashboard_semDados() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalProdutos", 0))
                .andExpect(model().attribute("totalLeads", 0));
    }
}
