package com.gpsit.sistema.shared.service;

import java.util.List;

public interface CrudService<T, ID> {

    List<T> listarTodos();

    T buscarPorId(ID id);

    T salvar(T entidade);

    void excluir(ID id);
}
