package br.com.fiap.cidadeemdia.exception;

import br.com.fiap.cidadeemdia.model.StatusOcorrencia;

/** Tentativa de mudar o status fora do ciclo de vida (ex.: REGISTRADA direto para CONCLUIDA). */
public class TransicaoStatusInvalidaException extends RegraNegocioException {

    public TransicaoStatusInvalidaException(StatusOcorrencia atual, StatusOcorrencia novo) {
        super("Não é possível mudar o status de " + atual + " para " + novo
                + ". Próximos status permitidos: " + (atual.proximosPermitidos().isEmpty()
                ? "nenhum (ocorrência encerrada)" : atual.proximosPermitidos()) + ".");
    }

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
