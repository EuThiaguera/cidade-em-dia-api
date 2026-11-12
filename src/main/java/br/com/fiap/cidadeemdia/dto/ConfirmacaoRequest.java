package br.com.fiap.cidadeemdia.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Um morador confirmando que o problema continua (POST /ocorrencias/{id}/confirmacoes).
 * No PUT /confirmacoes/{id} só o comentário é alterado.
 */
public class ConfirmacaoRequest {

    @NotNull(message = "O morador que está confirmando é obrigatório.")
    @Positive(message = "moradorId deve ser positivo.")
    private Long moradorId;

    @Size(max = 255, message = "O comentário deve ter no máximo 255 caracteres.")
    private String comentario;

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
