package com.gpsit.sistema.estoque.domain;

import com.gpsit.sistema.shared.exception.DadosInvalidosException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "codigo_barras", nullable = false, unique = true)
    private String codigoBarras;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeMedida unidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    protected Produto() {
    }

    public Produto(String nome, String codigoBarras, Categoria categoria,
            UnidadeMedida unidade, BigDecimal preco,
            int quantidade, int estoqueMinimo) {
        validarNome(nome);
        validarCodigoBarras(codigoBarras);
        validarPreco(preco);
        validarQuantidade(quantidade);
        validarEstoqueMinimo(estoqueMinimo);

        this.nome = nome.trim();
        this.codigoBarras = codigoBarras.trim();
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.unidade = Objects.requireNonNull(unidade, "Unidade de medida não pode ser nula");
        this.preco = preco;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    public Produto comQuantidade(int novaQuantidade) {
        Produto copia = new Produto(this.nome, this.codigoBarras, this.categoria,
                this.unidade, this.preco, novaQuantidade, this.estoqueMinimo);
        copia.id = this.id;
        return copia;
    }

    public Produto comPreco(BigDecimal novoPreco) {
        Produto copia = new Produto(this.nome, this.codigoBarras, this.categoria,
                this.unidade, novoPreco, this.quantidade, this.estoqueMinimo);
        copia.id = this.id;
        return copia;
    }

    public Produto comEstoqueMinimo(int novoEstoqueMinimo) {
        Produto copia = new Produto(this.nome, this.codigoBarras, this.categoria,
                this.unidade, this.preco, this.quantidade, novoEstoqueMinimo);
        copia.id = this.id;
        return copia;
    }

    public boolean estaBaixoDoEstoqueMinimo() {
        return quantidade < estoqueMinimo;
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
        validarNome(nome);
        this.nome = nome.trim();
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        validarCodigoBarras(codigoBarras);
        this.codigoBarras = codigoBarras.trim();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = Objects.requireNonNull(unidade, "Unidade de medida não pode ser nula");
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        validarPreco(preco);
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        validarQuantidade(quantidade);
        this.quantidade = quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        validarEstoqueMinimo(estoqueMinimo);
        this.estoqueMinimo = estoqueMinimo;
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DadosInvalidosException("Nome do produto não pode ser nulo ou vazio");
        }
        if (nome.trim().length() < 2) {
            throw new DadosInvalidosException("Nome do produto deve ter ao menos 2 caracteres");
        }
        if (nome.trim().length() > 100) {
            throw new DadosInvalidosException("Nome do produto não pode exceder 100 caracteres");
        }
    }

    private static void validarCodigoBarras(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new DadosInvalidosException("Código de barras não pode ser nulo ou vazio");
        }
    }

    private static void validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new DadosInvalidosException("Preço não pode ser nulo");
        }
        if (preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new DadosInvalidosException("Preço não pode ser negativo");
        }
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade < 0) {
            throw new DadosInvalidosException("Quantidade não pode ser negativa");
        }
    }

    private static void validarEstoqueMinimo(int estoqueMinimo) {
        if (estoqueMinimo < 0) {
            throw new DadosInvalidosException("Estoque mínimo não pode ser negativo");
        }
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Produto produto)) {
            return false;
        }
        return id != null && Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Produto[id=%d, nome='%s', categoria=%s, quantidade=%d %s, preço=R$%.2f]",
                id, nome, categoria, quantidade, unidade != null ? unidade.getSimbolo() : "", preco);
    }
}
