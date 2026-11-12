package br.com.fiap.cidadeemdia.dto;

import br.com.fiap.cidadeemdia.model.StatusOcorrencia;

import java.time.LocalDateTime;

/** Um item da linha do tempo da ocorrência, como a API devolve. */
public class AtualizacaoResponse {

    private Long id;
    private Long ocorrenciaId;
    private StatusOcorrencia statusAnterior;
    private StatusOcorrencia statusNovo;
    private String descricao;
    private String responsavel;
    private String urlEvidencia;
    private LocalDateTime dataAtualizacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public StatusOcorrencia getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusOcorrencia statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusOcorrencia getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusOcorrencia statusNovo) { this.statusNovo = statusNovo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getUrlEvidencia() { return urlEvidencia; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
