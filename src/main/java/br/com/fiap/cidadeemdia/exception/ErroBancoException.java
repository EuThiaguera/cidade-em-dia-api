package br.com.fiap.cidadeemdia.exception;

/**
 * Falha ao acessar o Oracle. Guarda a SQLException original para o log do servidor,
 * mas o cliente só recebe uma mensagem genérica (sem SQL nem stack trace). Vira HTTP 500.
 */
public class ErroBancoException extends RuntimeException {

    public ErroBancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
