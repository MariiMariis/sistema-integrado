package com.gpsit.sistema.estoque.web;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.Produto;
import com.gpsit.sistema.estoque.domain.UnidadeMedida;
import com.gpsit.sistema.estoque.service.EstoqueService;
import com.gpsit.sistema.estoque.service.ProdutoAtualizacaoDTO;
import com.gpsit.sistema.estoque.service.ProdutoCriacaoDTO;
import com.gpsit.sistema.shared.exception.NegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/produtos")
public class EstoqueController {

    private static final String REDIRECT_LISTA = "redirect:/produtos";
    private static final String VIEW_FORMULARIO = "estoque/formulario";
    private static final String VIEW_LISTA = "estoque/listar";
    private static final String VIEW_MOVIMENTACAO = "estoque/movimentacao";
    private static final String ATTR_SUCESSO = "mensagemSucesso";
    private static final String ATTR_ERRO = "mensagemErro";

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") boolean estoqueBaixo,
            Model model) {

        List<Produto> produtos;

        if (estoqueBaixo) {
            produtos = service.buscarComEstoqueBaixo();
            model.addAttribute("filtroAtivo", "Produtos com Estoque Baixo");
        } else if (categoria != null && !categoria.isBlank()) {
            try {
                Categoria cat = Categoria.valueOf(categoria);
                produtos = service.buscarPorCategoria(cat);
                model.addAttribute("filtroAtivo", cat.getDescricao());
            } catch (IllegalArgumentException e) {
                produtos = service.listarTodos();
            }
        } else {
            produtos = service.listarTodos();
        }

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("categoriaSelecionada", categoria);
        return VIEW_LISTA;
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(Model model) {
        model.addAttribute("produto", new ProdutoFormDTO());
        adicionarAtributosFormulario(model, "Novo Produto", "/produtos/novo");
        return VIEW_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Produto produto = service.buscarPorId(id);
            model.addAttribute("produto", preencherFormularioDeEdicao(produto));
            model.addAttribute("produtoId", id);
            adicionarAtributosFormulario(model, "Editar Produto", "/produtos/" + id + "/editar");
            return VIEW_FORMULARIO;
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    @GetMapping("/{id}/entrada")
    public String exibirFormularioEntrada(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        return prepararFormularioMovimentacao(id, model, redirectAttributes, "Registrar Entrada", "entrada");
    }

    @GetMapping("/{id}/saida")
    public String exibirFormularioSaida(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        return prepararFormularioMovimentacao(id, model, redirectAttributes, "Registrar Saída", "saida");
    }

    @PostMapping("/novo")
    public String criarProduto(@Valid @ModelAttribute("produto") ProdutoFormDTO form,
            BindingResult binding, Model model,
            RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            adicionarAtributosFormulario(model, "Novo Produto", "/produtos/novo");
            return VIEW_FORMULARIO;
        }

        try {
            service.criarProduto(new ProdutoCriacaoDTO(
                    form.getNome(), form.getCodigoBarras(), form.getCategoria(),
                    form.getUnidade(), form.getPreco(), form.getQuantidade(), form.getEstoqueMinimo()));

            redirectAttributes.addFlashAttribute(ATTR_SUCESSO,
                    "Produto '" + form.getNome() + "' cadastrado com sucesso!");
        } catch (NegocioException e) {
            model.addAttribute("erroNegocio", e.getMessage());
            adicionarAtributosFormulario(model, "Novo Produto", "/produtos/novo");
            return VIEW_FORMULARIO;
        }

        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/editar")
    public String atualizarProduto(@PathVariable Long id,
            @Valid @ModelAttribute("produto") ProdutoFormDTO form,
            BindingResult binding, Model model,
            RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            model.addAttribute("produtoId", id);
            adicionarAtributosFormulario(model, "Editar Produto", "/produtos/" + id + "/editar");
            return VIEW_FORMULARIO;
        }

        try {
            service.atualizarProduto(id,
                    new ProdutoAtualizacaoDTO(form.getNome(), form.getPreco(), form.getEstoqueMinimo()));
            redirectAttributes.addFlashAttribute(ATTR_SUCESSO, "Produto atualizado com sucesso!");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
        }

        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/remover")
    public String removerProduto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.excluir(id);
            redirectAttributes.addFlashAttribute(ATTR_SUCESSO, "Produto removido com sucesso!");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/entrada")
    public String registrarEntrada(@PathVariable Long id,
            @Valid @ModelAttribute("movimentacao") MovimentacaoFormDTO form,
            BindingResult binding, Model model,
            RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            return prepararFormularioMovimentacao(id, model, redirectAttributes, "Registrar Entrada", "entrada");
        }

        try {
            service.registrarEntrada(id, form.getQuantidade());
            redirectAttributes.addFlashAttribute(ATTR_SUCESSO,
                    "Entrada de " + form.getQuantidade() + " unidade(s) registrada!");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/saida")
    public String registrarSaida(@PathVariable Long id,
            @Valid @ModelAttribute("movimentacao") MovimentacaoFormDTO form,
            BindingResult binding, Model model,
            RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            return prepararFormularioMovimentacao(id, model, redirectAttributes, "Registrar Saída", "saida");
        }

        try {
            service.registrarSaida(id, form.getQuantidade());
            redirectAttributes.addFlashAttribute(ATTR_SUCESSO,
                    "Saída de " + form.getQuantidade() + " unidade(s) registrada!");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    private void adicionarAtributosFormulario(Model model, String titulo, String acao) {
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("unidades", UnidadeMedida.values());
        model.addAttribute("tituloPagina", titulo);
        model.addAttribute("acaoFormulario", acao);
    }

    private ProdutoFormDTO preencherFormularioDeEdicao(Produto produto) {
        ProdutoFormDTO form = new ProdutoFormDTO();
        form.setNome(produto.getNome());
        form.setCodigoBarras(produto.getCodigoBarras());
        form.setCategoria(produto.getCategoria());
        form.setUnidade(produto.getUnidade());
        form.setPreco(produto.getPreco());
        form.setQuantidade(produto.getQuantidade());
        form.setEstoqueMinimo(produto.getEstoqueMinimo());
        return form;
    }

    private String prepararFormularioMovimentacao(Long id, Model model,
            RedirectAttributes redirectAttributes,
            String titulo, String tipo) {
        try {
            Produto produto = service.buscarPorId(id);
            model.addAttribute("movimentacao", new MovimentacaoFormDTO());
            model.addAttribute("produto", produto);
            model.addAttribute("tituloPagina", titulo);
            model.addAttribute("tipoMovimentacao", tipo);
            model.addAttribute("acaoFormulario", "/produtos/" + id + "/" + tipo);
            return VIEW_MOVIMENTACAO;
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
            return REDIRECT_LISTA;
        }
    }
}
