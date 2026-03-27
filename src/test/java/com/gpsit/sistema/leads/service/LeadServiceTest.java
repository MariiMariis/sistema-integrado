package com.gpsit.sistema.leads.service;

import com.gpsit.sistema.leads.domain.Lead;
import com.gpsit.sistema.leads.domain.StatusLead;
import com.gpsit.sistema.leads.repository.LeadRepository;
import com.gpsit.sistema.shared.exception.RecursoNaoEncontradoException;
import com.gpsit.sistema.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("LeadService")
class LeadServiceTest {

    @Autowired
    private LeadService service;

    @Autowired
    private LeadRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private Lead criarLeadNoServico(String nome, String email) {
        return service.salvar(new Lead(nome, email, "11999999999", StatusLead.NOVO, "Teste"));
    }

    @Nested
    @DisplayName("salvar")
    class Salvar {

        @Test
        @DisplayName("Salva novo lead com sucesso")
        void novoLead_salvaSucesso() {
            Lead lead = criarLeadNoServico("João Silva", "joao@email.com");
            assertNotNull(lead.getId());
            assertEquals("João Silva", lead.getNome());
        }

        @Test
        @DisplayName("Lança exceção para e-mail duplicado em novo lead")
        void emailDuplicadoNovoLead_lancaExcecao() {
            criarLeadNoServico("João", "duplicado@email.com");
            Lead novoLead = new Lead("Maria", "duplicado@email.com", "11888888888",
                    StatusLead.CONTATADO, "");
            RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                    () -> service.salvar(novoLead));
            assertEquals("Já existe um lead cadastrado com este e-mail.", ex.getMessage());
        }

        @Test
        @DisplayName("Permite atualizar lead mantendo mesmo email")
        void atualizarLeadMesmoEmail_naoLancaExcecao() {
            Lead criado = criarLeadNoServico("João", "joao@email.com");
            criado.setNome("João Atualizado");
            Lead atualizado = service.salvar(criado);
            assertEquals("João Atualizado", atualizado.getNome());
        }

        @Test
        @DisplayName("Lança exceção ao atualizar lead com email de outro lead")
        void atualizarLeadEmailDeOutroLead_lancaExcecao() {
            criarLeadNoServico("João", "joao@email.com");
            Lead maria = criarLeadNoServico("Maria", "maria@email.com");
            maria.setEmail("joao@email.com");
            assertThrows(RegraNegocioException.class, () -> service.salvar(maria));
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("Retorna lead para ID existente")
        void idExistente_retornaLead() {
            Lead criado = criarLeadNoServico("Ana", "ana@email.com");
            Lead resultado = service.buscarPorId(criado.getId());
            assertEquals("Ana", resultado.getNome());
        }

        @Test
        @DisplayName("Lança exceção para ID inexistente")
        void idInexistente_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.buscarPorId(999L));
        }
    }

    @Nested
    @DisplayName("excluir")
    class Excluir {

        @Test
        @DisplayName("Exclui lead existente com sucesso")
        void leadExistente_excluiSucesso() {
            Lead criado = criarLeadNoServico("Carlos", "carlos@email.com");
            service.excluir(criado.getId());
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.buscarPorId(criado.getId()));
        }

        @Test
        @DisplayName("Lança exceção ao excluir ID inexistente")
        void idInexistente_lancaExcecao() {
            assertThrows(RecursoNaoEncontradoException.class,
                    () -> service.excluir(999L));
        }
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("Retorna lista vazia quando não há leads")
        void semLeads_retornaVazio() {
            assertTrue(service.listarTodos().isEmpty());
        }

        @Test
        @DisplayName("Retorna todos os leads cadastrados")
        void comLeads_retornaTodos() {
            criarLeadNoServico("Lead1", "lead1@email.com");
            criarLeadNoServico("Lead2", "lead2@email.com");
            assertEquals(2, service.listarTodos().size());
        }
    }
}
