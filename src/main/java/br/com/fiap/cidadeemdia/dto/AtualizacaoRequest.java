package br.com.fiap.cidadeemdia.dto;

import br.com.fiap.cidadeemdia.model.StatusOcorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados de uma atualização feita pela gestão.
 *  - POST /ocorrencias/{id}/atualizacoes: "status" é o NOVO status da ocorrência.
 *  - PUT /atualizacoes/{id}: só os textos podem mudar; o status precisa ser o mesmo já registrado.
 */
public class AtualizacaoRequest {

    @NotNull(message = "O novo status é obrigatório (EM_ANALISE, EM_EXECUCAO, CONCLUIDA ou DESCARTADA).")
    private StatusOcorrencia status;

    @NotBlank(message = "A descrição da atualização é obrigatória.")
    @Size(min = 5, max = 500, message = "A descrição deve ter entre 5 e 500 caracteres.")
    private String descricao;

    @NotBlank(message = "O responsável é obrigatório.")
    @Size(max = 100, message = "O responsável deve ter no máximo 100 caracteres.")
    private String responsavel;

    @Size(max = 255, message = "A URL da evidência deve ter no máximo 255 caracteres.")
    @Pattern(regexp = "^https?://.+", message = "A URL da evidência deve começar com http:// ou https://")
    private String urlEvidencia;

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getUrlEvidencia() { return urlEvidencia; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }
}
