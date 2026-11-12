package br.com.fiap.cidadeemdia.model;

import java.time.LocalDateTime;

/**
 * Um morador confirmando que o problema de uma ocorrência continua existindo.
 * É assim que vários relatos do mesmo problema viram UMA ocorrência com força.
 * Uma ocorrência possui várias confirmações (relacionamento 1:N).
 */
public class Confirmacao {

    private Long id;
    private Long ocorrenciaId;
    private Morador morador;
    private String comentario;
    private LocalDateTime dataConfirmacao;

    public Confirmacao() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public Morador getMorador() { return morador; }
    public void setMorador(Morador morador) { this.morador = morador; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
}
