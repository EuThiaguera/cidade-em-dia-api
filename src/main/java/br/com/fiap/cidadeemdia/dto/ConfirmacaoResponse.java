package br.com.fiap.cidadeemdia.dto;

import java.time.LocalDateTime;

/** Uma confirmação, como a API devolve. */
public class ConfirmacaoResponse {

    private Long id;
    private Long ocorrenciaId;
    private Long moradorId;
    private String moradorNome;
    private String comentario;
    private LocalDateTime dataConfirmacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

    public String getMoradorNome() { return moradorNome; }
    public void setMoradorNome(String moradorNome) { this.moradorNome = moradorNome; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
}
