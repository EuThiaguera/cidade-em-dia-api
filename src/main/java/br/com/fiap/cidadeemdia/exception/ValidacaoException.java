package br.com.fiap.cidadeemdia.exception;

/**
 * Dado da requisição inválido que o Bean Validation não consegue checar sozinho
 * (ex.: categoriaId que não existe no banco). Vira HTTP 400.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
