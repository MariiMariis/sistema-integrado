package com.gpsit.sistema.config;

import com.gpsit.sistema.estoque.service.EstoqueService;
import com.gpsit.sistema.leads.domain.StatusLead;
import com.gpsit.sistema.leads.service.LeadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EstoqueService estoqueService;
    private final LeadService leadService;

    public DashboardController(EstoqueService estoqueService, LeadService leadService) {
        this.estoqueService = estoqueService;
        this.leadService = leadService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var produtos = estoqueService.listarTodos();
        var leads = leadService.listarTodos();

        model.addAttribute("totalProdutos", produtos.size());
        model.addAttribute("produtosEstoqueBaixo", estoqueService.buscarComEstoqueBaixo().size());
        model.addAttribute("totalLeads", leads.size());
        model.addAttribute("leadsNovos", leads.stream()
                .filter(l -> l.getStatus() == StatusLead.NOVO).count());
        model.addAttribute("leadsConvertidos", leads.stream()
                .filter(l -> l.getStatus() == StatusLead.CONVERTIDO).count());

        return "dashboard";
    }
}

