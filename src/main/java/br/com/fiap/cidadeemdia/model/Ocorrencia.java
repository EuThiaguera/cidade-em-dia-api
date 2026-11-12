package br.com.fiap.cidadeemdia.model;

import br.com.fiap.cidadeemdia.exception.TransicaoStatusInvalidaException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Problema urbano registrado por um morador em um ponto do mapa.
 *
 * É a entidade central do domínio:
 *  - pertence a uma Categoria e a um Morador (lado N de dois relacionamentos 1:N);
 *  - possui várias Atualizações (linha do tempo) e várias Confirmações (1:N).
 *
 * Além dos dados, ela tem COMPORTAMENTO: sabe se está aberta, calcula a distância até outro
 * ponto e controla a própria mudança de status.
 */
public class Ocorrencia {

    /** Raio médio da Terra em metros, usado na fórmula de Haversine. */
    private static final double RAIO_TERRA_METROS = 6_371_000;

    private Long id;
    private Categoria categoria;
    private Morador morador;
    private String titulo;
    private String descricao;
    private Double latitude;
    private Double longitude;
    private String endereco;
    private String urlFoto;
    private StatusOcorrencia status;
    private LocalDateTime dataAbertura;

    /** Quantidade de confirmações (calculada na consulta, não é coluna da tabela). */
    private int totalConfirmacoes;

    public Ocorrencia() {
    }

    // ------------------------------------------------------------------
    //  Comportamento do domínio
    // ------------------------------------------------------------------

    public boolean estaAberta() {
        return status != null && !status.isEncerrado();
    }

    /**
     * Distância em metros entre esta ocorrência e outro ponto (fórmula de Haversine).
     * Usada para descobrir se um novo registro é, na verdade, o mesmo problema.
     */
    public double distanciaEmMetrosAte(double outraLatitude, double outraLongitude) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(outraLatitude);
        double deltaLat = Math.toRadians(outraLatitude - latitude);
        double deltaLon = Math.toRadians(outraLongitude - longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAIO_TERRA_METROS * c;
    }

    /**
     * Muda o status respeitando o ciclo de vida.
     * Para concluir é obrigatório informar a evidência (foto do "depois").
     *
     * @return o status anterior, para ser gravado no histórico
     */
    public StatusOcorrencia mudarStatus(StatusOcorrencia novoStatus, String urlEvidencia) {
        if (!status.podeMudarPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(status, novoStatus);
        }
        if (novoStatus == StatusOcorrencia.CONCLUIDA && (urlEvidencia == null || urlEvidencia.isBlank())) {
            throw new TransicaoStatusInvalidaException(
                    "Para concluir a ocorrência é obrigatório informar a urlEvidencia (foto do depois).");
        }
        StatusOcorrencia anterior = this.status;
        this.status = novoStatus;
        return anterior;
    }

    /** Dias desde a abertura. Ajuda a gestão a enxergar o que está parado há mais tempo. */
    public long getDiasEmAberto() {
        if (dataAbertura == null) {
            return 0;
        }
        return Duration.between(dataAbertura, LocalDateTime.now()).toDays();
    }

    // ------------------------------------------------------------------
    //  Getters e setters
    // ------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Morador getMorador() { return morador; }
    public void setMorador(Morador morador) { this.morador = morador; }

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

    public int getTotalConfirmacoes() { return totalConfirmacoes; }
    public void setTotalConfirmacoes(int totalConfirmacoes) { this.totalConfirmacoes = totalConfirmacoes; }
}
