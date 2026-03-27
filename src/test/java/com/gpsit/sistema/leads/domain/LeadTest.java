package com.gpsit.sistema.leads.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lead")
class LeadTest {

    static Lead leadValido() {
        return new Lead("João Silva", "joao@email.com", "11999999999",
                StatusLead.NOVO, "Observação de teste");
    }

    @Nested
    @DisplayName("Métodos de cópia imutável")
    class CopiaImutavel {

        @Test
        @DisplayName("comNome retorna novo objeto sem alterar o original")
        void comNome_retornaNovaInstancia() {
            Lead original = leadValido();
            original.setId(1L);
            Lead atualizado = original.comNome("Maria Santos");

            assertNotSame(original, atualizado);
            assertEquals("João Silva", original.getNome());
            assertEquals("Maria Santos", atualizado.getNome());
            assertEquals(original.getId(), atualizado.getId());
            assertEquals(original.getEmail(), atualizado.getEmail());
        }

        @Test
        @DisplayName("comEmail retorna novo objeto sem alterar o original")
        void comEmail_retornaNovaInstancia() {
            Lead original = leadValido();
            original.setId(1L);
            Lead atualizado = original.comEmail("novo@email.com");

            assertNotSame(original, atualizado);
            assertEquals("joao@email.com", original.getEmail());
            assertEquals("novo@email.com", atualizado.getEmail());
            assertEquals(original.getId(), atualizado.getId());
        }

        @Test
        @DisplayName("comTelefone retorna novo objeto sem alterar o original")
        void comTelefone_retornaNovaInstancia() {
            Lead original = leadValido();
            original.setId(1L);
            Lead atualizado = original.comTelefone("11888888888");

            assertNotSame(original, atualizado);
            assertEquals("11999999999", original.getTelefone());
            assertEquals("11888888888", atualizado.getTelefone());
            assertEquals(original.getId(), atualizado.getId());
        }

        @Test
        @DisplayName("comStatus retorna novo objeto sem alterar o original")
        void comStatus_retornaNovaInstancia() {
            Lead original = leadValido();
            original.setId(1L);
            Lead atualizado = original.comStatus(StatusLead.CONVERTIDO);

            assertNotSame(original, atualizado);
            assertEquals(StatusLead.NOVO, original.getStatus());
            assertEquals(StatusLead.CONVERTIDO, atualizado.getStatus());
            assertEquals(original.getId(), atualizado.getId());
        }

        @Test
        @DisplayName("comObservacoes retorna novo objeto sem alterar o original")
        void comObservacoes_retornaNovaInstancia() {
            Lead original = leadValido();
            original.setId(1L);
            Lead atualizado = original.comObservacoes("Nova observação");

            assertNotSame(original, atualizado);
            assertEquals("Observação de teste", original.getObservacoes());
            assertEquals("Nova observação", atualizado.getObservacoes());
            assertEquals(original.getId(), atualizado.getId());
        }
    }

    @Nested
    @DisplayName("equals e hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("Leads com mesmo ID são iguais")
        void mesmoId_saoIguais() {
            Lead l1 = leadValido();
            l1.setId(1L);
            Lead l2 = new Lead("Outro", "outro@email.com", "11111111111",
                    StatusLead.PERDIDO, "Outro");
            l2.setId(1L);
            assertEquals(l1, l2);
            assertEquals(l1.hashCode(), l2.hashCode());
        }

        @Test
        @DisplayName("Leads com IDs diferentes não são iguais")
        void idsDiferentes_naoSaoIguais() {
            Lead l1 = leadValido();
            l1.setId(1L);
            Lead l2 = leadValido();
            l2.setId(2L);
            assertNotEquals(l1, l2);
        }

        @Test
        @DisplayName("Lead sem ID não é igual a lead com ID")
        void semIdComId_naoSaoIguais() {
            Lead l1 = leadValido();
            Lead l2 = leadValido();
            l2.setId(1L);
            assertNotEquals(l1, l2);
        }

        @Test
        @DisplayName("Lead é igual a si mesmo")
        void mesmoObjeto_saoIguais() {
            Lead lead = leadValido();
            lead.setId(1L);
            assertEquals(lead, lead);
        }

        @Test
        @DisplayName("Lead não é igual a null")
        void comparaComNull_naoEhIgual() {
            Lead lead = leadValido();
            lead.setId(1L);
            assertNotEquals(null, lead);
        }

        @Test
        @DisplayName("Lead não é igual a tipo diferente")
        void comparaComOutroTipo_naoEhIgual() {
            Lead lead = leadValido();
            lead.setId(1L);
            assertNotEquals("string", lead);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString contém informações relevantes")
        void toStringContemInformacoes() {
            Lead l = leadValido();
            l.setId(1L);
            String resultado = l.toString();
            assertTrue(resultado.contains("João Silva"));
            assertTrue(resultado.contains("joao@email.com"));
            assertTrue(resultado.contains("Novo"));
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersESetters() {
            Lead lead = new Lead();
            lead.setId(1L);
            lead.setNome("Teste");
            lead.setEmail("teste@email.com");
            lead.setTelefone("11999990000");
            lead.setStatus(StatusLead.QUALIFICADO);
            lead.setObservacoes("Obs");

            assertEquals(1L, lead.getId());
            assertEquals("Teste", lead.getNome());
            assertEquals("teste@email.com", lead.getEmail());
            assertEquals("11999990000", lead.getTelefone());
            assertEquals(StatusLead.QUALIFICADO, lead.getStatus());
            assertEquals("Obs", lead.getObservacoes());
        }
    }

    @Nested
    @DisplayName("StatusLead enum")
    class StatusLeadEnum {

        @Test
        @DisplayName("Todos os status possuem descrição")
        void todosStatusComDescricao() {
            for (StatusLead status : StatusLead.values()) {
                assertNotNull(status.getDescricao());
                assertFalse(status.getDescricao().isEmpty());
            }
        }

        @Test
        @DisplayName("toString retorna a descrição")
        void toStringRetornaDescricao() {
            assertEquals("Novo", StatusLead.NOVO.toString());
            assertEquals("Convertido", StatusLead.CONVERTIDO.toString());
        }
    }
}
