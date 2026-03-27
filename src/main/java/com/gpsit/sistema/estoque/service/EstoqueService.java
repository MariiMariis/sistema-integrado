package com.gpsit.sistema.estoque.service;

import com.gpsit.sistema.estoque.domain.Categoria;
import com.gpsit.sistema.estoque.domain.Produto;
import com.gpsit.sistema.estoque.repository.ProdutoRepository;
import com.gpsit.sistema.shared.exception.DadosInvalidosException;
import com.gpsit.sistema.shared.exception.RecursoNaoEncontradoException;
import com.gpsit.sistema.shared.exception.RegraNegocioException;
import com.gpsit.sistema.shared.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class EstoqueService implements CrudService<Produto, Long> {

    private final ProdutoRepository repository;

    public EstoqueService(ProdutoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repositório não pode ser nulo");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado com o ID: " + id));
    }

    @Override
    public Produto salvar(Produto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo");
        validarCodigoBarrasUnico(produto);
        return repository.save(produto);
    }

    @Override
    public void excluir(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Produto produto = buscarPorId(id);
        repository.delete(produto);
    }

    public Produto criarProduto(ProdutoCriacaoDTO dto) {
        Objects.requireNonNull(dto, "Dados de criação não podem ser nulos");

        if (repository.existsByCodigoBarras(dto.codigoBarras())) {
            throw new RegraNegocioException(
                    "Código de barras '" + dto.codigoBarras() + "' já está cadastrado.");
        }

        Produto produto = new Produto(
                dto.nome(), dto.codigoBarras(), dto.categoria(),
                dto.unidade(), dto.preco(), dto.quantidadeInicial(), dto.estoqueMinimo());

        return repository.save(produto);
    }

    public Produto atualizarProduto(Long id, ProdutoAtualizacaoDTO dto) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Objects.requireNonNull(dto, "Dados de atualização não podem ser nulos");

        Produto atual = buscarPorId(id);

        if (dto.novoNome() != null) {
            atual.setNome(dto.novoNome());
        }
        if (dto.novoPreco() != null) {
            atual.setPreco(dto.novoPreco());
        }
        if (dto.novoEstoqueMinimo() != null) {
            atual.setEstoqueMinimo(dto.novoEstoqueMinimo());
        }

        return repository.save(atual);
    }

    public void registrarEntrada(Long id, int quantidade) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        validarQuantidadeMovimentacao(quantidade, "entrada");

        Produto produto = buscarPorId(id);
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        repository.save(produto);
    }

    public void registrarSaida(Long id, int quantidade) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        validarQuantidadeMovimentacao(quantidade, "saída");

        Produto produto = buscarPorId(id);

        if (produto.getQuantidade() < quantidade) {
            throw new RegraNegocioException(
                    String.format("Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                            produto.getNome(), produto.getQuantidade(), quantidade));
        }

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        repository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorCategoria(Categoria categoria) {
        Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        return repository.findByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarComEstoqueBaixo() {
        return repository.findAll().stream()
                .filter(Produto::estaBaixoDoEstoqueMinimo)
                .toList();
    }

    private void validarCodigoBarrasUnico(Produto produto) {
        if (produto.getId() == null) {
            if (repository.existsByCodigoBarras(produto.getCodigoBarras())) {
                throw new RegraNegocioException(
                        "Código de barras '" + produto.getCodigoBarras() + "' já está cadastrado.");
            }
        } else {
            if (repository.existsByCodigoBarrasAndIdNot(produto.getCodigoBarras(), produto.getId())) {
                throw new RegraNegocioException(
                        "Código de barras '" + produto.getCodigoBarras() + "' já está cadastrado.");
            }
        }
    }

    private void validarQuantidadeMovimentacao(int quantidade, String tipoMovimento) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException(
                    "Quantidade de " + tipoMovimento + " deve ser maior que zero. Recebido: " + quantidade);
        }
    }
}
