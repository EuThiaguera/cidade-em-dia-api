package br.com.fiap.cidadeemdia.service;

import br.com.fiap.cidadeemdia.dao.AtualizacaoDAO;
import br.com.fiap.cidadeemdia.dto.AtualizacaoRequest;
import br.com.fiap.cidadeemdia.dto.AtualizacaoResponse;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.RegraNegocioException;
import br.com.fiap.cidadeemdia.exception.TransicaoStatusInvalidaException;
import br.com.fiap.cidadeemdia.model.Atualizacao;
import br.com.fiap.cidadeemdia.model.Ocorrencia;
import br.com.fiap.cidadeemdia.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras da linha do tempo (CICLO DE VIDA):
 *  - toda mudança de status passa por aqui e precisa respeitar o StatusOcorrencia;
 *  - CONCLUIDA exige evidência (foto do depois);
 *  - o status registrado numa atualização não pode ser editado depois;
 *  - só a ÚLTIMA atualização pode ser desfeita (e a ocorrência volta ao status anterior).
 */
@Service
public class AtualizacaoService {

    private final AtualizacaoDAO atualizacaoDAO;
    private final OcorrenciaService ocorrenciaService;
    private final ModelMapper mapper;

    public AtualizacaoService(AtualizacaoDAO atualizacaoDAO, OcorrenciaService ocorrenciaService, ModelMapper mapper) {
        this.atualizacaoDAO = atualizacaoDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.mapper = mapper;
    }

    public List<AtualizacaoResponse> listarPorOcorrencia(Long ocorrenciaId) {
        ocorrenciaService.buscarEntidade(ocorrenciaId); // 404 se a ocorrência não existir
        return atualizacaoDAO.listarPorOcorrencia(ocorrenciaId).stream()
                .map(a -> mapper.map(a, AtualizacaoResponse.class))
                .toList();
    }

    public AtualizacaoResponse registrar(Long ocorrenciaId, AtualizacaoRequest request) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarEntidade(ocorrenciaId);

        // O próprio domínio valida a transição (e lança 409 se for inválida)
        StatusOcorrencia anterior = ocorrencia.mudarStatus(request.getStatus(), request.getUrlEvidencia());

        Atualizacao atualizacao = mapper.map(request, Atualizacao.class);
        atualizacao.setOcorrenciaId(ocorrenciaId);
        atualizacao.setStatusAnterior(anterior);
        atualizacao.setStatusNovo(request.getStatus());
        atualizacao.setDataAtualizacao(LocalDateTime.now());

        atualizacaoDAO.inserirMudandoStatus(atualizacao);
        return mapper.map(atualizacao, AtualizacaoResponse.class);
    }

    public AtualizacaoResponse buscar(Long id) {
        return mapper.map(buscarEntidade(id), AtualizacaoResponse.class);
    }

    public AtualizacaoResponse atualizar(Long id, AtualizacaoRequest request) {
        Atualizacao atualizacao = buscarEntidade(id);

        if (request.getStatus() != atualizacao.getStatusNovo()) {
            throw new RegraNegocioException("O status de uma atualização faz parte do histórico e não pode ser "
                    + "alterado. Para mudar o status, registre uma nova atualização.");
        }
        if (atualizacao.getStatusNovo() == StatusOcorrencia.CONCLUIDA
                && (request.getUrlEvidencia() == null || request.getUrlEvidencia().isBlank())) {
            throw new TransicaoStatusInvalidaException(
                    "Uma atualização de conclusão precisa manter a urlEvidencia.");
        }

        atualizacao.setDescricao(request.getDescricao());
        atualizacao.setResponsavel(request.getResponsavel());
        atualizacao.setUrlEvidencia(request.getUrlEvidencia());
        atualizacaoDAO.atualizar(atualizacao);
        return mapper.map(atualizacao, AtualizacaoResponse.class);
    }

    public void remover(Long id) {
        Atualizacao atualizacao = buscarEntidade(id);
        Long ultimaId = atualizacaoDAO.buscarIdDaUltima(atualizacao.getOcorrenciaId()).orElse(null);

        if (!atualizacao.getId().equals(ultimaId)) {
            throw new RegraNegocioException("Só a atualização mais recente da ocorrência pode ser removida "
                    + "(a mais recente é a de id " + ultimaId + ").");
        }
        atualizacaoDAO.removerRevertendoStatus(atualizacao);
    }

    private Atualizacao buscarEntidade(Long id) {
        return atualizacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atualização", id));
    }
}
