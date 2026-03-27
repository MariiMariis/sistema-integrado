# Sistema Integrado — Controle de Estoque e Gestão de Leads

![CI Status](https://github.com/MariiMariis/sistema-integrado/actions/workflows/ci.yml/badge.svg)
![Security](https://github.com/MariiMariis/sistema-integrado/actions/workflows/security.yml/badge.svg)
![Deploy](https://github.com/MariiMariis/sistema-integrado/actions/workflows/deploy.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green)
![Coverage](https://img.shields.io/badge/Coverage-%E2%89%A590%25-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

> Sistema integrado que unifica o **Controle de Estoque** de mercado e a **Gestão de Leads** em uma única aplicação Spring Boot com interface web (Thymeleaf), banco H2, CI/CD com GitHub Actions, e testes automatizados (JUnit 5 + Selenium).

---

## Índice

- [Pré-requisitos](#pré-requisitos)
- [Executar Localmente](#executar-localmente)
- [Arquitetura do Sistema](#arquitetura-do-sistema)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Refatorações Aplicadas](#refatorações-aplicadas)
- [Banco de Dados](#banco-de-dados)
- [CI/CD com GitHub Actions](#cicd-com-github-actions)
- [Workflows](#workflows)
- [Ambientes de Deploy](#ambientes-de-deploy)
- [Estratégia de Testes](#estratégia-de-testes)
- [Segurança (SAST/DAST)](#segurança-sastdast)
- [Monitoramento e Depuração](#monitoramento-e-depuração)

---

## Pré-requisitos

- **JDK 17** (ou superior)
- **Git** (para versionamento)

> O Gradle Wrapper está incluído no projeto — não é necessário instalar o Gradle separadamente.

---

## Executar Localmente

### 1. Compilar o projeto

```bash
./gradlew build -x test
```

### 2. Executar os testes (unitários + integração)

```bash
./gradlew test
```

Relatório HTML disponível em: `build/reports/tests/test/index.html`

### 3. Executar testes Selenium (E2E)

```bash
./gradlew seleniumTest
```

Os testes Selenium utilizam **HtmlUnitDriver** (headless) e validam:
- Dashboard carrega e exibe contadores
- CRUD de produtos via formulário
- CRUD de leads via formulário
- Health check do Actuator

### 4. Verificar cobertura de testes (JaCoCo ≥ 90%)

```bash
./gradlew jacocoTestReport
```

Relatório HTML em: `build/reports/jacoco/test/html/index.html`

### 5. Análise estática de código (Checkstyle)

```bash
./gradlew checkstyleMain
```

Relatório HTML em: `build/reports/checkstyle/main.html`

O Checkstyle valida as seguintes regras de código limpo:
- Convenções de nomenclatura (pacotes, classes, métodos, variáveis)
- Uso obrigatório de chaves em blocos `if`, `for`, `while`
- Importações não utilizadas ou redundantes
- Boas práticas: `equals`/`hashCode`, `switch` com `default`, etc.

### 6. Análise de segurança estática (SpotBugs)

```bash
./gradlew spotbugsMain
```

Relatório HTML em: `build/reports/spotbugs/main.html`

### 7. Verificação completa (build + testes + cobertura + Checkstyle)

```bash
./gradlew check
```

O comando `check` executa automaticamente:
1. Compilação
2. Análise estática (Checkstyle)
3. Análise de bugs (SpotBugs)
4. Testes unitários e de integração
5. Relatório JaCoCo
6. Verificação de cobertura mínima (≥ 90%)

### 8. Executar a aplicação

```bash
./gradlew bootRun
```

Acesse: [http://localhost:8080](http://localhost:8080)

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 3.4.3                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Dashboard    │  │   Estoque    │  │     Leads        │  │
│  │  Controller   │  │  Controller  │  │   Controller     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────────┘  │
│         │                 │                  │              │
│  ┌──────┴─────────────────┴──────────────────┴───────────┐  │
│  │                   Service Layer                        │  │
│  │    EstoqueService (CrudService<Produto, Long>)        │  │
│  │    LeadService    (CrudService<Lead, Long>)           │  │
│  └──────┬─────────────────┬──────────────────────────────┘  │
│         │                 │                                 │
│  ┌──────┴─────────────────┴──────────────────────────────┐  │
│  │             Spring Data JPA Repositories               │  │
│  │    ProdutoRepository        LeadRepository             │  │
│  └──────┬─────────────────┬──────────────────────────────┘  │
│         │                 │                                 │
│  ┌──────┴─────────────────┴──────────────────────────────┐  │
│  │                 H2 Database (In-Memory)                │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Shared: NegocioException hierarchy, CrudService<T>   │  │
│  │  GlobalExceptionHandler, Actuator Health Check        │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Princípios arquiteturais:**
- **Camadas bem definidas**: Controller → Service → Repository
- **Interface genérica**: `CrudService<T, ID>` implementada por ambos serviços
- **Hierarquia de exceções unificada**: `NegocioException` → `RecursoNaoEncontradoException`, `DadosInvalidosException`, `RegraNegocioException`
- **Imutabilidade**: Métodos `com*()` nas entidades para transformações sem efeitos colaterais

---

## Estrutura do Projeto

```
sistema-integrado/
├── .github/workflows/
│   ├── ci.yml                       # Pipeline CI (build, test, quality, selenium)
│   ├── deploy.yml                   # Pipeline de deploy (dev, staging, prod)
│   └── security.yml                 # Análise SAST/DAST
├── config/checkstyle/checkstyle.xml # Regras do Checkstyle
├── build.gradle                     # Configuração Gradle + plugins
├── src/main/java/com/gpsit/sistema/
│   ├── SistemaIntegradoApplication  # Classe principal
│   ├── config/                      # Dashboard + configurações
│   ├── shared/                      # Exceções e interfaces reutilizáveis
│   │   ├── exception/               # NegocioException (hierarquia)
│   │   └── service/                 # CrudService<T, ID>
│   ├── estoque/                     # Módulo de Estoque
│   │   ├── domain/                  # Produto, Categoria, UnidadeMedida
│   │   ├── repository/              # ProdutoRepository (JPA)
│   │   ├── service/                 # EstoqueService + DTOs (records)
│   │   └── web/                     # EstoqueController + Form DTOs
│   └── leads/                       # Módulo de Leads
│       ├── domain/                  # Lead, StatusLead
│       ├── repository/              # LeadRepository (JPA)
│       ├── service/                 # LeadService
│       └── web/                     # LeadController
├── src/main/resources/
│   ├── application.properties       # Configuração (H2, JPA, Actuator)
│   ├── templates/                   # Templates Thymeleaf
│   └── static/css/                  # CSS unificado
└── src/test/java/
    ├── estoque/                     # Testes unitários + integração
    ├── leads/                       # Testes unitários + integração
    ├── integracao/                   # Testes de integração do sistema
    └── selenium/                    # Testes E2E (Selenium + HtmlUnit)
```

---

## Refatorações Aplicadas

| Princípio | Antes | Depois |
|---|---|---|
| **Hierarquia de exceções** | 4 hierarquias separadas | 1 hierarquia unificada: `NegocioException` → subclasses |
| **Interface reutilizável** | Métodos CRUD duplicados | `CrudService<T, ID>` implementado por ambos |
| **Persistência** | Estoque in-memory; Leads JPA | Ambos usam Spring Data JPA + H2 |
| **Exception handler** | Dois handlers separados | Um único `GlobalExceptionHandler` unificado |
| **Código morto** | `FalhaSimuladaConfig` | Removido |
| **SRP** | Controller com lógica de negócio | Controller → Service → Repository |
| **Imutabilidade** | DTOs mutáveis; setters em entidades | Records (`ProdutoCriacaoDTO`); métodos `com*()` em `Produto` e `Lead` |
| **Tipos de valor** | Strings para categoria/status | Enums: `Categoria`, `UnidadeMedida`, `StatusLead` |
| **Guard clauses** | Comparações por string no Dashboard | Comparação direta com enum (`StatusLead.NOVO`) |
| **Análise estática** | Nenhuma | Checkstyle + SpotBugs integrados ao build e CI |
| **Clean Code** | Nomes inconsistentes | Nomenclatura padronizada em português |

---

## Banco de Dados

O sistema usa **H2 em memória** (`jdbc:h2:mem:sistemadb`). Os dados são recriados a cada reinicialização.

---

## CI/CD com GitHub Actions

### Workflows

O projeto conta com **3 workflows** no GitHub Actions:

#### 1. CI — Sistema Integrado (`ci.yml`)

Pipeline principal de integração contínua com **5 jobs**:

| Job | Descrição |
|-----|-----------|
| **Build** | Compila o projeto e gera o JAR |
| **Qualidade** | Checkstyle + SpotBugs (SAST) |
| **Testes** | Testes unitarios/integracao + JaCoCo (>= 90%) |
| **Selenium** | Testes E2E pos-deploy headless |
| **Resumo** | Tabela consolidada de resultados |

**Triggers:**

| Evento | Descrição |
|--------|-----------|
| `push` para `main` ou `develop` | Executa automaticamente |
| `pull_request` para `main` | Executa ao abrir/atualizar PR |
| `workflow_dispatch` | Execução manual |

#### 2. Deploy — Sistema Integrado (`deploy.yml`)

Pipeline de deploy com **4 jobs** e **3 ambientes**:

| Job | Ambiente | Aprovacao |
|-----|----------|-----------|
| **Deploy Dev** | `dev` | Automatico |
| **Deploy Staging** | `staging` | Automatico |
| **Testes Pos-Deploy** | staging | Automatico (Selenium) |
| **Deploy Production** | `production` | Requer aprovacao manual |

**Triggers:**

| Evento | Descrição |
|--------|-----------|
| `workflow_run` (CI sucesso) | Após CI passar na `main` |
| `release` publicado | Deploy automático ao publicar release |
| `workflow_dispatch` | Manual com seleção de ambiente |

**Segredos e variáveis utilizados:**
- `secrets.DEPLOY_TOKEN` — Token de autenticação para deploy
- `secrets.DATABASE_URL` — URL do banco de dados de produção
- `vars.APP_URL` — URL da aplicação por ambiente
- `id-token: write` — Permissão OIDC para autenticação segura com nuvem

#### 3. Security — Análise SAST/DAST (`security.yml`)

| Job | Descricao |
|-----|-----------|
| **SAST** | SpotBugs — analise estatica do codigo-fonte |
| **DAST** | OWASP ZAP — scan de vulnerabilidades na aplicacao em execucao |
| **Resumo** | Tabela consolidada de resultados de seguranca |

**Triggers:** push/PR para `main`, execução semanal (segunda 06:00 UTC), manual.

---

## Ambientes de Deploy

| Ambiente | Proteção | Descrição |
|----------|----------|-----------|
| **dev** | Nenhuma | Deploy automático após CI passar. Usado para desenvolvimento e testes iniciais. |
| **staging** | Testes Selenium | Deploy automático + testes E2E obrigatórios antes de promover para produção. |
| **production** | Aprovação manual | Requer um ou mais revisores aprovarem na interface do GitHub Actions. Configurar em: Settings → Environments → production → Required reviewers. |

### Configuração de OIDC

Para integrar com provedores de nuvem (AWS, GCP, Azure), o workflow `deploy.yml` inclui:
- Permissão `id-token: write` habilitada
- Placeholders para ações de autenticação OIDC (descomente conforme o provedor)

---

## Estratégia de Testes

### Pirâmide de Testes

| Nível | Ferramenta | Descrição | Quantidade |
|-------|-----------|-----------|------------|
| **Unitários** | JUnit 5, jqwik | Validação de domínio, lógica de negócio, DTOs | ~40 testes |
| **Integração** | Spring Boot Test, MockMvc | Controllers, Services com banco real (H2) | ~25 testes |
| **E2E (Selenium)** | Selenium + HtmlUnit | Fluxos reais via browser headless | 7 testes |

### Testes Pós-Deploy

Os testes Selenium são executados em **dois momentos**:
1. **No CI** (job `selenium`): valida após build, antes de qualquer deploy
2. **No Deploy** (job `post-deploy-tests`): valida após deploy em staging, antes de promover para produção

Cobertura mínima exigida: **≥ 90%** (verificado pelo JaCoCo no CI)

### Comandos de teste

```bash
# Todos os testes (exceto Selenium)
./gradlew test

# Apenas testes Selenium
./gradlew seleniumTest

# Verificação completa com cobertura
./gradlew check
```

---

## Segurança (SAST/DAST)

### SAST — SpotBugs

Análise estática que detecta:
- Null pointer dereferences
- Inconsistências em equals/hashCode
- Vulnerabilidades de segurança (SQL injection, XSS)
- Performance issues

```bash
./gradlew spotbugsMain
```

### DAST — OWASP ZAP

Scan baseline contra a aplicação em execução que verifica:
- Cabeçalhos HTTP de segurança
- Vulnerabilidades de configuração
- Cross-site scripting (XSS)
- Injeção de SQL

Executado automaticamente no workflow `security.yml` (semanal + push para main).

---

## Monitoramento e Depuração

### Logs Personalizados nos Workflows

Todos os workflows utilizam recursos avançados de logging:

- **`::group::`/`::endgroup::`** — Agrupa logs em seções colapsáveis para facilitar leitura
- **`::notice::`** — Destaca mensagens importantes nos logs
- **`$GITHUB_STEP_SUMMARY`** — Gera resumos em Markdown visíveis na página de execução do workflow

### Como Monitorar no GitHub

1. Acesse o repositório → aba **Actions**
2. Selecione o workflow desejado (CI, Deploy, Security)
3. Clique em uma execução para ver:
   - **Logs** — saída de cada step com seções colapsáveis
   - **Summary** — resumo Markdown com tabela de resultados
   - **Artifacts** — relatórios para download (testes, cobertura, segurança)

### Como Interpretar os Resultados

- **Verde** — todos os jobs passaram
- **Vermelho** — algum job falhou. Clique para ver qual step falhou e os logs detalhados

### Badges

Os badges no topo deste README refletem o status em tempo real:
- **CI Status** — resultado do último build + testes
- **Security** — resultado da última análise SAST/DAST
- **Deploy** — status do último deploy

---

## Runners: GitHub-hosted vs Self-hosted

### Runners Hospedados pelo GitHub (usado neste projeto)

O pipeline usa `runs-on: ubuntu-latest`, que é um **runner hospedado pelo GitHub**.

**Vantagens:**
- Não requer configuração — o GitHub fornece a máquina virtual
- Ambiente limpo a cada execução (sem cache "sujo")
- Manutenção zero (GitHub cuida das atualizações)
- Ferramentas pré-instaladas (Git, Docker, Node, etc.)

**Limitações:**
- 2.000 minutos/mês gratuitos (plano Free)
- Sem acesso a recursos de rede interna

### Runners Auto-hospedados (Self-hosted)

São máquinas próprias registradas como runners no GitHub.

**Quando usar:**
- Quando a aplicação precisa de acesso a recursos internos (banco de dados, VPN)
- Quando precisa de hardware específico (GPU, alta memória)
- Quando o limite de minutos gratuitos é insuficiente

**Como configurar (se necessário):**
1. Vá em **Settings** → **Actions** → **Runners** → **New self-hosted runner**
2. Siga as instruções para instalar o agente na máquina
3. No workflow YAML, troque `runs-on: ubuntu-latest` por `runs-on: self-hosted`

**Para este projeto**, o runner hospedado pelo GitHub é a melhor opção — é suficiente para build Java + testes e não requer manutenção.
