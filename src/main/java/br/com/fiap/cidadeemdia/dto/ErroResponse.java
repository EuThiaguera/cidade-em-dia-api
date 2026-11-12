package br.com.fiap.cidadeemdia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato ÚNICO de erro da API. Todo erro (400, 404, 405, 409, 500) sai neste JSON,
 * sem stack trace e sem detalhes do SQL.
 *
 * Os campos "campos" e "ocorrenciaExistenteId" só aparecem quando fazem sentido
 * (@JsonInclude NON_NULL esconde os nulos).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        String caminho,
        LocalDateTime timestamp,
        List<CampoInvalido> campos,
        Long ocorrenciaExistenteId) {

    /** Um campo que falhou no Bean Validation. */
    public record CampoInvalido(String campo, String mensagem) {
    }

    public ErroResponse(int status, String erro, String mensagem, String caminho) {
        this(status, erro, mensagem, caminho, LocalDateTime.now(), null, null);
    }
}
