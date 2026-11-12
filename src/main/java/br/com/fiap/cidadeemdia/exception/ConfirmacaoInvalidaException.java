package br.com.fiap.cidadeemdia.exception;

/** Confirmação que fere as regras: repetida, do próprio autor ou em ocorrência encerrada. */
public class ConfirmacaoInvalidaException extends RegraNegocioException {

    public ConfirmacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
