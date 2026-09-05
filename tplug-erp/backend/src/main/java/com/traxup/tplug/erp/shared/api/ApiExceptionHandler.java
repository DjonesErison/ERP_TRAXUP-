package com.traxup.tplug.erp.shared.api;

import com.traxup.tplug.erp.shared.exception.AutenticacaoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail recursoNaoEncontrado(RecursoNaoEncontradoException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Recurso nao encontrado");
        return problem;
    }

    @ExceptionHandler(RecursoConflitanteException.class)
    public ProblemDetail recursoConflitante(RecursoConflitanteException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Conflito de recurso");
        return problem;
    }

    @ExceptionHandler(AutenticacaoException.class)
    public ProblemDetail autenticacao(AutenticacaoException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problem.setTitle("Falha de autenticacao");
        return problem;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Regra de negocio violada");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Dados invalidos");
        problem.setDetail("A requisicao possui campos invalidos");

        Map<String, String> erros = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                erros.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("erros", erros);

        return problem;
    }
}
