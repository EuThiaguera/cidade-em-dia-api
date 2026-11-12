package br.com.fiap.cidadeemdia.exception;

/** Tentativa de remover um registro que ainda tem filhos (ex.: categoria com ocorrências). */
public class RegistroEmUsoException extends RegraNegocioException {

    public RegistroEmUsoException(String mensagem) {
        super(mensagem);
    }
}
