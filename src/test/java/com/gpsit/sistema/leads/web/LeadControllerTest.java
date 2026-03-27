package com.gpsit.sistema.leads.web;

import com.gpsit.sistema.leads.domain.Lead;
import com.gpsit.sistema.leads.domain.StatusLead;
import com.gpsit.sistema.leads.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("LeadController")
class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private Lead criarLeadNoBanco(String nome, String email) {
        Lead lead = new Lead(nome, email, "11999999999", StatusLead.NOVO, "Teste");
        return repository.save(lead);
    }

    @Test
    @DisplayName("GET /leads — retorna página de listagem")
    void listar_retornaView() throws Exception {
        mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/lista"))
                .andExpect(model().attributeExists("leads"));
    }

    @Test
    @DisplayName("GET /leads/novo — exibe formulário de novo lead")
    void novo_retornaFormulario() throws Exception {
        mockMvc.perform(get("/leads/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/formulario"))
                .andExpect(model().attributeExists("lead", "statusValues"));
    }

    @Test
    @DisplayName("POST /leads/salvar — salva lead com sucesso")
    void salvar_sucesso_redireciona() throws Exception {
        mockMvc.perform(post("/leads/salvar")
                        .param("nome", "Carlos Silva")
                        .param("email", "carlos@email.com")
                        .param("telefone", "11999999999")
                        .param("status", "NOVO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("POST /leads/salvar — validação falha retorna formulário")
    void salvar_validacaoFalha_retornaFormulario() throws Exception {
        mockMvc.perform(post("/leads/salvar")
                        .param("nome", "")
                        .param("email", "email-invalido")
                        .param("telefone", "123")
                        .param("status", "NOVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/formulario"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("lead", "nome", "email", "telefone"));
    }

    @Test
    @DisplayName("POST /leads/salvar — e-mail duplicado retorna formulário com erro")
    void salvar_emailDuplicado_retornaErro() throws Exception {
        criarLeadNoBanco("Admin", "admin@email.com");

        mockMvc.perform(post("/leads/salvar")
                        .param("nome", "Outro Admin")
                        .param("email", "admin@email.com")
                        .param("telefone", "11222222222")
                        .param("status", "NOVO"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/formulario"))
                .andExpect(model().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /leads/{id}/editar — exibe formulário de edição")
    void editar_retornaFormulario() throws Exception {
        Lead lead = criarLeadNoBanco("Ana", "ana@email.com");

        mockMvc.perform(get("/leads/" + lead.getId() + "/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("leads/formulario"))
                .andExpect(model().attributeExists("lead", "statusValues"));
    }

    @Test
    @DisplayName("GET /leads/{id}/excluir — exclui lead com sucesso")
    void excluir_sucesso() throws Exception {
        Lead lead = criarLeadNoBanco("Bruno", "bruno@email.com");

        mockMvc.perform(get("/leads/" + lead.getId() + "/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"))
                .andExpect(flash().attributeExists("mensagemSucesso"));
    }

    @Test
    @DisplayName("GET /leads/{id}/editar — ID inexistente redireciona com erro")
    void editar_idInexistente_redireciona() throws Exception {
        mockMvc.perform(get("/leads/999/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /leads/{id}/excluir — ID inexistente redireciona com erro")
    void excluir_idInexistente_redireciona() throws Exception {
        mockMvc.perform(get("/leads/999/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }
}
