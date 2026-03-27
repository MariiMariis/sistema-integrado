package com.gpsit.sistema.leads.service;

import com.gpsit.sistema.leads.domain.Lead;
import com.gpsit.sistema.leads.repository.LeadRepository;
import com.gpsit.sistema.shared.exception.RecursoNaoEncontradoException;
import com.gpsit.sistema.shared.exception.RegraNegocioException;
import com.gpsit.sistema.shared.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class LeadService implements CrudService<Lead, Long> {

    private final LeadRepository repository;

    public LeadService(LeadRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repositório não pode ser nulo");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lead> listarTodos() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Lead buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Lead não encontrado com o ID: " + id));
    }

    @Override
    public Lead salvar(Lead lead) {
        Objects.requireNonNull(lead, "Lead não pode ser nulo");
        validarEmailUnico(lead);
        return repository.save(lead);
    }

    @Override
    public void excluir(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Lead lead = buscarPorId(id);
        repository.delete(lead);
    }

    private void validarEmailUnico(Lead lead) {
        if (lead.getId() == null) {
            if (repository.findByEmail(lead.getEmail()).isPresent()) {
                throw new RegraNegocioException("Já existe um lead cadastrado com este e-mail.");
            }
        } else {
            if (repository.existsByEmailAndIdNot(lead.getEmail(), lead.getId())) {
                throw new RegraNegocioException("Já existe um lead cadastrado com este e-mail.");
            }
        }
    }
}
