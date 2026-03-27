package com.gpsit.sistema.leads.web;

import com.gpsit.sistema.leads.domain.Lead;
import com.gpsit.sistema.leads.domain.StatusLead;
import com.gpsit.sistema.leads.service.LeadService;
import com.gpsit.sistema.shared.exception.NegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/leads")
public class LeadController {

    private static final String REDIRECT_LISTA = "redirect:/leads";
    private static final String VIEW_LISTA = "leads/lista";
    private static final String VIEW_FORMULARIO = "leads/formulario";
    private static final String ATTR_SUCESSO = "mensagemSucesso";
    private static final String ATTR_ERRO = "mensagemErro";

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("leads", service.listarTodos());
        return VIEW_LISTA;
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("lead", new Lead());
        model.addAttribute("statusValues", StatusLead.values());
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("lead") Lead lead,
            BindingResult result, Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("statusValues", StatusLead.values());
            return VIEW_FORMULARIO;
        }

        try {
            service.salvar(lead);
            attributes.addFlashAttribute(ATTR_SUCESSO, "Lead salvo com sucesso!");
        } catch (NegocioException ex) {
            model.addAttribute("statusValues", StatusLead.values());
            model.addAttribute(ATTR_ERRO, ex.getMessage());
            return VIEW_FORMULARIO;
        }

        return REDIRECT_LISTA;
    }

    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Lead lead = service.buscarPorId(id);
            model.addAttribute("lead", lead);
            model.addAttribute("statusValues", StatusLead.values());
            return VIEW_FORMULARIO;
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
            return REDIRECT_LISTA;
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            service.excluir(id);
            attributes.addFlashAttribute(ATTR_SUCESSO, "Lead removido com sucesso!");
        } catch (NegocioException e) {
            attributes.addFlashAttribute(ATTR_ERRO, e.getMessage());
        }
        return REDIRECT_LISTA;
    }
}
