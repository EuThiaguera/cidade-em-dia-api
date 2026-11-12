package br.com.fiap.cidadeemdia.handler;

import br.com.fiap.cidadeemdia.dto.ErroResponse;
import br.com.fiap.cidadeemdia.exception.ErroBancoException;
import br.com.fiap.cidadeemdia.exception.LoginInvalidoException;
import br.com.fiap.cidadeemdia.exception.OcorrenciaDuplicadaException;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.RegraNegocioException;
import br.com.fiap.cidadeemdia.exception.ValidacaoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tratamento GLOBAL de erros. Qualquer exceção lançada em controller, service ou DAO
 * chega aqui e vira um ErroResponse em JSON com o status HTTP correto:
 *
 *   400 - Bean Validation, JSON mal formado, tipo errado, referência inexistente
 *   404 - id não encontrado / rota inexistente
 *   405 - método HTTP não suportado na rota
 *   409 - regra de negócio violada
 *   500 - erro inesperado ou de banco (mensagem genérica, detalhes só no log)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------- 400 -------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarBeanValidation(MethodArgumentNotValidException e,
                                                             HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResponse.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();

        ErroResponse corpo = new ErroResponse(400, "Bad Request", "Existem campos inválidos na requisição.",
                req.getRequestURI(), LocalDateTime.now(), campos, null);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(ValidacaoException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, e.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "JSON inválido. Verifique a sintaxe, os tipos dos campos e os valores de status "
                        + "(REGISTRADA, EM_ANALISE, EM_EXECUCAO, CONCLUIDA, DESCARTADA).", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoErrado(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + e.getName() + "'.", req);
    }

    // ------------------------------- 401 -------------------------------

    @ExceptionHandler(LoginInvalidoException.class)
    public ResponseEntity<ErroResponse> tratarLogin(LoginInvalidoException e, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, e.getMessage(), req);
    }

    // ------------------------------- 404 -------------------------------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada.", req);
    }

    // ------------------------------- 405 / 415 -------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodo(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + e.getMethod() + " não é suportado nesta rota.", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Envie o corpo como JSON (Content-Type: application/json).", req);
    }

    // ------------------------------- 409 -------------------------------

    /** Caso especial: devolve também o id da ocorrência existente para o front redirecionar. */
    @ExceptionHandler(OcorrenciaDuplicadaException.class)
    public ResponseEntity<ErroResponse> tratarDuplicada(OcorrenciaDuplicadaException e, HttpServletRequest req) {
        ErroResponse corpo = new ErroResponse(409, "Conflict", e.getMessage(), req.getRequestURI(),
                LocalDateTime.now(), null, e.getOcorrenciaExistenteId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    /** Todas as outras regras de negócio (herdam de RegraNegocioException). */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    // ------------------------------- 500 -------------------------------

    @ExceptionHandler(ErroBancoException.class)
    public ResponseEntity<ErroResponse> tratarBanco(ErroBancoException e, HttpServletRequest req) {
        log.error("Erro de banco de dados em {}", req.getRequestURI(), e); // detalhes só no log do servidor
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados. Tente novamente.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarGenerico(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.", req);
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI()));
    }
}
