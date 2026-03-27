package com.gpsit.sistema.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                    HttpServletRequest request) {
        return montarPaginaErro(404, "Recurso não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ModelAndView tratarDadosInvalidos(DadosInvalidosException ex,
                                             HttpServletRequest request) {
        return montarPaginaErro(400, "Dados Inválidos", ex.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView tratarRegraNegocio(RegraNegocioException ex,
                                           HttpServletRequest request) {
        return montarPaginaErro(400, "Regra de Negócio Violada", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView tratarArgumentoInvalido(IllegalArgumentException ex,
                                                HttpServletRequest request) {
        return montarPaginaErro(400, "Dados Inválidos", "Dados inválidos: " + ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView tratarErroInesperado(Exception ex, HttpServletRequest request) {
        return montarPaginaErro(500, "Erro Interno no Servidor",
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", request);
    }

    private ModelAndView montarPaginaErro(int status, String erro, String mensagem,
                                           HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("erro");
        mav.addObject("timestamp", new Date());
        mav.addObject("status", status);
        mav.addObject("erro", erro);
        mav.addObject("mensagem", mensagem);
        mav.addObject("caminho", request.getRequestURI());
        return mav;
    }
}
