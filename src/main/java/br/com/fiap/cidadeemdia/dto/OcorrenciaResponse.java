package br.com.fiap.cidadeemdia.dto;

import br.com.fiap.cidadeemdia.model.StatusOcorrencia;

import java.time.LocalDateTime;

/**
 * O que a API devolve sobre uma ocorrência.
 * Achata categoria e morador (categoriaNome, moradorNome) e traz indicadores calculados
 * (totalConfirmacoes e diasEmAberto) que o front usa no mapa e no painel.
 */
public class OcorrenciaResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private Double latitude;
    private Double longitude;
    private String endereco;
    private String urlFoto;
    private StatusOcorrencia status;
    private LocalDateTime dataAbertura;
    private Long categoriaId;
    private String categoriaNome;
    private Long moradorId;
    private String moradorNome;
    private int totalConfirmacoes;
    private long diasEmAberto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

    public String getMoradorNome() { return moradorNome; }
    public void setMoradorNome(String moradorNome) { this.moradorNome = moradorNome; }

    public int getTotalConfirmacoes() { return totalConfirmacoes; }
    public void setTotalConfirmacoes(int totalConfirmacoes) { this.totalConfirmacoes = totalConfirmacoes; }

    public long getDiasEmAberto() { return diasEmAberto; }
    public void setDiasEmAberto(long diasEmAberto) { this.diasEmAberto = diasEmAberto; }
}
