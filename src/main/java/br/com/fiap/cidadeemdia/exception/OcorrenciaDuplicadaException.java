package br.com.fiap.cidadeemdia.exception;

/**
 * Regra principal do Cidade em Dia: já existe uma ocorrência ABERTA, da MESMA categoria,
 * muito perto do ponto informado. Em vez de duplicar, o morador deve confirmar a existente.
 * Carrega o id da ocorrência existente para o front poder redirecionar o usuário.
 */
public class OcorrenciaDuplicadaException extends RegraNegocioException {

    private final Long ocorrenciaExistenteId;

    public OcorrenciaDuplicadaException(Long ocorrenciaExistenteId, double distanciaMetros) {
        super(String.format(
                "Já existe uma ocorrência aberta desta categoria a %.0f metros (id %d). "
                        + "Confirme a ocorrência existente em vez de registrar outra.",
                distanciaMetros, ocorrenciaExistenteId));
        this.ocorrenciaExistenteId = ocorrenciaExistenteId;
    }

    public Long getOcorrenciaExistenteId() {
        return ocorrenciaExistenteId;
    }
}
