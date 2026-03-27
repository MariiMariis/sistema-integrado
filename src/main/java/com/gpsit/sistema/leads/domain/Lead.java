package com.gpsit.sistema.leads.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O e-mail não pode estar em branco")
    @Email(message = "O formato do e-mail é inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "O telefone não pode estar em branco")
    @Size(min = 10, max = 20, message = "O telefone deve ter entre 10 e 20 caracteres")
    @Column(nullable = false, length = 20)
    private String telefone;

    @NotNull(message = "O status do lead deve ser informado")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLead status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public Lead() {
    }

    public Lead(String nome, String email, String telefone, StatusLead status, String observacoes) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.status = status;
        this.observacoes = observacoes;
    }

    public Lead comNome(String novoNome) {
        Lead copia = new Lead(novoNome, this.email, this.telefone, this.status, this.observacoes);
        copia.id = this.id;
        return copia;
    }

    public Lead comEmail(String novoEmail) {
        Lead copia = new Lead(this.nome, novoEmail, this.telefone, this.status, this.observacoes);
        copia.id = this.id;
        return copia;
    }

    public Lead comTelefone(String novoTelefone) {
        Lead copia = new Lead(this.nome, this.email, novoTelefone, this.status, this.observacoes);
        copia.id = this.id;
        return copia;
    }

    public Lead comStatus(StatusLead novoStatus) {
        Lead copia = new Lead(this.nome, this.email, this.telefone, novoStatus, this.observacoes);
        copia.id = this.id;
        return copia;
    }

    public Lead comObservacoes(String novasObservacoes) {
        Lead copia = new Lead(this.nome, this.email, this.telefone, this.status, novasObservacoes);
        copia.id = this.id;
        return copia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusLead getStatus() {
        return status;
    }

    public void setStatus(StatusLead status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Lead lead)) {
            return false;
        }
        return id != null && Objects.equals(id, lead.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Lead[id=%d, nome='%s', email='%s', status=%s]",
                id, nome, email, status);
    }
}
