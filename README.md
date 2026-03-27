# Sistema Integrado — Manual de Execução

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

### 2. Executar os testes

```bash
./gradlew test
```

Relatório HTML disponível em: `build/reports/tests/test/index.html`

### 3. Verificar cobertura de testes (JaCoCo)

```bash
./gradlew jacocoTestReport
```

Relatório HTML em: `build/reports/jacoco/test/html/index.html`

### 4. Análise estática de código (Checkstyle)

```bash
./gradlew checkstyleMain
```

Relatório HTML em: `build/reports/checkstyle/main.html`

O Checkstyle valida as seguintes regras de código limpo:
- Convenções de nomenclatura (pacotes, classes, métodos, variáveis)
- Uso obrigatório de chaves em blocos `if`, `for`, `while`
- Importações não utilizadas ou redundantes
- Boas práticas: `equals`/`hashCode`, `switch` com `default`, etc.

### 5. Verificação completa (build + testes + cobertura + Checkstyle)

```bash
./gradlew check
```

O comando `check` executa automaticamente:
1. Compilação
2. Análise estática (Checkstyle)
3. Testes unitários e de integração
4. Relatório JaCoCo
5. Verificação de cobertura mínima (≥ 85%)

### 6. Executar a aplicação

```bash
./gradlew bootRun
```

Acesse: [http://localhost:8080](http://localhost:8080)

---

## Estrutura do Projeto

```
sistema-integrado/
├── .github/workflows/ci.yml        # Pipeline CI/CD
├── config/checkstyle/checkstyle.xml # Regras do Checkstyle
├── build.gradle                     # Configuração Gradle
├── src/main/java/com/gpsit/sistema/
│   ├── SistemaIntegradoApplication  # Classe principal
│   ├── config/                      # Dashboard + configurações
│   ├── shared/                      # Exceções e interfaces reutilizáveis
│   │   ├── exception/               # NegocioException (hierarquia)
│   │   └── service/                 # CrudService<T, ID>
│   ├── estoque/                     # Módulo de Estoque
│   │   ├── domain/                  # Produto, Categoria, UnidadeMedida
│   │   ├── repository/              # ProdutoRepository (JPA)
│   │   ├── service/                 # EstoqueService + DTOs
│   │   └── web/                     # EstoqueController + Form DTOs
│   └── leads/                       # Módulo de Leads
│       ├── domain/                  # Lead, StatusLead
│       ├── repository/              # LeadRepository (JPA)
│       ├── service/                 # LeadService
│       └── web/                     # LeadController
├── src/main/resources/
│   ├── application.properties       # Configuração (H2, JPA, Thymeleaf)
│   ├── templates/                   # Templates Thymeleaf
│   └── static/css/                  # CSS unificado
└── src/test/java/                   # Testes (JUnit 5, MockMvc, jqwik)
```

---

## Banco de Dados

O sistema usa **H2 em memória** (`jdbc:h2:mem:sistemadb`). Os dados são recriados a cada reinicialização.

---

## CI/CD com GitHub Actions

O arquivo `.github/workflows/ci.yml` define o pipeline de integração contínua.

### Etapas do Pipeline

1. **Checkout** — clona o repositório (`actions/checkout@v4`)
2. **Setup JDK 17** — configura o ambiente Java (`actions/setup-java@v4`)
3. **Cache Gradle** — acelera builds subsequentes (`actions/cache@v4`)
4. **Build** — compila o projeto
5. **Análise estática (Checkstyle)** — valida regras de código limpo
6. **Testes** — executa todos os testes unitários e de integração
7. **JaCoCo Report** — gera o relatório de cobertura
8. **Verificação de Cobertura** — falha se < 85%
9. **Upload de Artefatos** — salva relatórios como artefatos baixáveis:
   - `test-report` — relatório de execução dos testes
   - `jacoco-report` — relatório de cobertura de código
   - `checkstyle-report` — relatório de análise estática

### Triggers (Quando o Pipeline Executa)

| Evento | Descrição |
|--------|-----------|
| `push` para `main` ou `develop` | Executa automaticamente a cada push |
| `pull_request` para `main` | Executa ao abrir/atualizar um PR |
| `workflow_dispatch` | Execução manual pela aba Actions |

### Como Monitorar o Pipeline no GitHub

1. Acesse o repositório no GitHub
2. Clique na aba **Actions**
3. Selecione o workflow **CI — Sistema Integrado**
4. Clique em uma execução para ver os detalhes:
   - **Logs** — saída de cada step do pipeline
   - **Artefatos** — relatórios para download na seção "Artifacts"
5. Para disparar manualmente: clique em **Run workflow** → **Run workflow**

### Como Interpretar os Resultados

- ✅ **Verde** — todos os steps passaram (build, Checkstyle, testes, cobertura)
- ❌ **Vermelho** — algum step falhou. Clique na execução para ver:
  - Qual step falhou (indicado com ❌)
  - Os logs detalhados com a causa do erro

### Como Baixar os Artefatos (Relatórios)

1. Na aba **Actions**, clique na execução desejada
2. Role até a seção **Artifacts**
3. Clique no artefato para baixar o ZIP:
   - `test-report.zip` → abra `index.html` para ver os resultados dos testes
   - `jacoco-report.zip` → abra `index.html` para ver a cobertura
   - `checkstyle-report.zip` → abra `main.html` para ver as violações

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
3. No `ci.yml`, troque `runs-on: ubuntu-latest` por `runs-on: self-hosted`

**Para este projeto**, o runner hospedado pelo GitHub é a melhor opção — é suficiente para build Java + testes e não requer manutenção.

---

## Refatorações Aplicadas

| Princípio | Antes | Depois |
|---|---|---|
| **Hierarquia de exceções** | 4 hierarquias separadas (`EstoqueException`, `ProdutoNaoEncontradoException`, `RegraNegocioExcecao`, `LeadNaoEncontradoExcecao`) | 1 hierarquia unificada: `NegocioException` → `RecursoNaoEncontradoException`, `DadosInvalidosException`, `RegraNegocioException` |
| **Interface reutilizável** | Métodos CRUD duplicados em ambos serviços | `CrudService<T, ID>` implementado por ambos |
| **Persistência** | Estoque: repositório in-memory; Leads: JPA | Ambos usam Spring Data JPA + H2 |
| **Exception handler** | `GlobalExceptionHandler` + `TratadorErrosGlobais` | Um único `GlobalExceptionHandler` unificado |
| **Código morto** | `FalhaSimuladaConfig` (simulação de falhas) | Removido |
| **SRP** | Controller manipulava lógica de negócio | Controller → Service → Repository (camadas claras) |
| **Imutabilidade** | DTOs como classes mutáveis | Records Java (`ProdutoCriacaoDTO`, `ProdutoAtualizacaoDTO`) |
| **Valores primitivos → objetos** | Strings para categoria/unidade/status | Enums: `Categoria`, `UnidadeMedida`, `StatusLead` |
| **Análise estática** | Nenhuma | Checkstyle integrado ao build e CI |
| **Clean Code** | Nomes inconsistentes entre módulos | Nomenclatura padronizada em português |
