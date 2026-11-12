package br.com.fiap.cidadeemdia.exception;

/** Valor que precisa ser único já está em uso (ex.: e-mail de morador, nome de categoria). */
public class DadoDuplicadoException extends RegraNegocioException {

    public DadoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
