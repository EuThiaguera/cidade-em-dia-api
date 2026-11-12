package br.com.fiap.cidadeemdia.exception;

/**
 * Base de todas as violações de regra de negócio do domínio. Vira HTTP 409 (Conflict).
 * As exceções específicas abaixo herdam desta, então o handler trata todas de uma vez.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
