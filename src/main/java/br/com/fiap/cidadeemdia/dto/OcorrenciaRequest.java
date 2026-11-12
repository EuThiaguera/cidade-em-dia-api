package br.com.fiap.cidadeemdia.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados para registrar/editar uma ocorrência (POST e PUT /ocorrencias).
 * No PUT o morador autor não muda; o status também não (só muda por uma Atualização).
 */
public class OcorrenciaRequest {

    @NotNull(message = "A categoria é obrigatória.")
    @Positive(message = "categoriaId deve ser positivo.")
    private Long categoriaId;

    @NotNull(message = "O morador é obrigatório.")
    @Positive(message = "moradorId deve ser positivo.")
    private Long moradorId;

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres.")
    private String descricao;

    @NotNull(message = "A latitude é obrigatória.")
    @DecimalMin(value = "-90.0", message = "A latitude deve estar entre -90 e 90.")
    @DecimalMax(value = "90.0", message = "A latitude deve estar entre -90 e 90.")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória.")
    @DecimalMin(value = "-180.0", message = "A longitude deve estar entre -180 e 180.")
    @DecimalMax(value = "180.0", message = "A longitude deve estar entre -180 e 180.")
    private Double longitude;

    @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.")
    private String endereco;

    @Size(max = 255, message = "A URL da foto deve ter no máximo 255 caracteres.")
    @Pattern(regexp = "^https?://.+", message = "A URL da foto deve começar com http:// ou https://")
    private String urlFoto;

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

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
}
