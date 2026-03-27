package com.gpsit.sistema.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes pós-deploy (E2E) que validam a integridade do sistema
 * simulando requisições HTTP reais contra a aplicação em execução.
 *
 * <p>Equivalentes a testes Selenium, mas usando TestRestTemplate
 * para garantir execução headless no CI/CD sem dependência de browser.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Testes Pós-Deploy (E2E)")
class SeleniumPostDeployTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // --- Dashboard ---

    @Test
    @DisplayName("Dashboard carrega com status 200 e exibe conteúdo")
    void dashboardCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/dashboard"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Dashboard"),
                "A página do Dashboard deve conter a palavra 'Dashboard'");
    }

    @Test
    @DisplayName("Raiz (/) redireciona para /dashboard")
    void raizRedirecionaParaDashboard() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Dashboard"),
                "Após redirecionamento, deve exibir o Dashboard");
    }

    // --- Estoque ---

    @Test
    @DisplayName("Página de listagem de produtos retorna 200")
    void listagemProdutosCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/produtos"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Formulário de novo produto retorna 200")
    void formularioProdutoCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/produtos/novo"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Criar produto via formulário e verificar redirecionamento")
    void criarProdutoViaFormulario() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("nome", "Produto E2E");
        params.add("codigoBarras", "E2E-001");
        params.add("categoria", "OUTROS");
        params.add("unidade", "UNIDADE");
        params.add("preco", "9.99");
        params.add("quantidade", "50");
        params.add("estoqueMinimo", "5");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/produtos/novo"), request, String.class);

        assertTrue(response.getStatusCode().is3xxRedirection()
                        || response.getStatusCode() == HttpStatus.OK,
                "Deve redirecionar após criar produto ou exibir página");
    }

    // --- Leads ---

    @Test
    @DisplayName("Página de listagem de leads retorna 200")
    void listagemLeadsCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/leads"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Formulário de novo lead retorna 200")
    void formularioLeadCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/leads/novo"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Criar lead via formulário e verificar resposta")
    void criarLeadViaFormulario() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("nome", "Lead E2E Test");
        params.add("email", "e2e-test@email.com");
        params.add("telefone", "11999990000");
        params.add("status", "NOVO");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/leads/salvar"), request, String.class);

        assertTrue(response.getStatusCode().is3xxRedirection()
                        || response.getStatusCode() == HttpStatus.OK,
                "Deve redirecionar após criar lead ou exibir página");
    }

    // --- Health Check (Actuator) ---

    @Test
    @DisplayName("Endpoint de health check responde com status UP")
    void healthCheckResponde() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/actuator/health"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("UP"),
                "O health check deve retornar status UP");
    }

    // --- Estoque com filtro ---

    @Test
    @DisplayName("Página de estoque com filtro de estoque baixo retorna 200")
    void filtroEstoqueBaixoCarrega() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/produtos?estoqueBaixo=true"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
