package br.com.fiap.cidadeemdia.service;

import br.com.fiap.cidadeemdia.dao.ConfirmacaoDAO;
import br.com.fiap.cidadeemdia.dao.MoradorDAO;
import br.com.fiap.cidadeemdia.dto.ConfirmacaoRequest;
import br.com.fiap.cidadeemdia.dto.ConfirmacaoResponse;
import br.com.fiap.cidadeemdia.exception.ConfirmacaoInvalidaException;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.ValidacaoException;
import br.com.fiap.cidadeemdia.model.Confirmacao;
import br.com.fiap.cidadeemdia.model.Morador;
import br.com.fiap.cidadeemdia.model.Ocorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras da CONFIRMAÇÃO COMUNITÁRIA:
 *  - só ocorrências abertas podem ser confirmadas;
 *  - o autor não confirma a própria ocorrência;
 *  - cada morador confirma a mesma ocorrência uma única vez.
 */
@Service
public class ConfirmacaoService {

    private final ConfirmacaoDAO confirmacaoDAO;
    private final MoradorDAO moradorDAO;
    private final OcorrenciaService ocorrenciaService;
    private final ModelMapper mapper;

    public ConfirmacaoService(ConfirmacaoDAO confirmacaoDAO, MoradorDAO moradorDAO,
                              OcorrenciaService ocorrenciaService, ModelMapper mapper) {
        this.confirmacaoDAO = confirmacaoDAO;
        this.moradorDAO = moradorDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.mapper = mapper;
    }

    public List<ConfirmacaoResponse> listarPorOcorrencia(Long ocorrenciaId) {
        ocorrenciaService.buscarEntidade(ocorrenciaId); // 404 se a ocorrência não existir
        return confirmacaoDAO.listarPorOcorrencia(ocorrenciaId).stream()
                .map(this::paraResponse)
                .toList();
    }

    public ConfirmacaoResponse confirmar(Long ocorrenciaId, ConfirmacaoRequest request) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarEntidade(ocorrenciaId);
        Morador morador = moradorDAO.buscarPorId(request.getMoradorId())
                .orElseThrow(() -> new ValidacaoException("Morador informado não existe: id " + request.getMoradorId()));

        if (!ocorrencia.estaAberta()) {
            throw new ConfirmacaoInvalidaException(
                    "A ocorrência está " + ocorrencia.getStatus() + " e não recebe mais confirmações.");
        }
        if (ocorrencia.getMorador().getId().equals(morador.getId())) {
            throw new ConfirmacaoInvalidaException("O autor da ocorrência não pode confirmar o próprio registro.");
        }
        if (confirmacaoDAO.existe(ocorrenciaId, morador.getId())) {
            throw new ConfirmacaoInvalidaException("Este morador já confirmou esta ocorrência.");
        }

        Confirmacao confirmacao = new Confirmacao();
        confirmacao.setOcorrenciaId(ocorrenciaId);
        confirmacao.setMorador(morador);
        confirmacao.setComentario(request.getComentario());
        confirmacao.setDataConfirmacao(LocalDateTime.now());
        confirmacaoDAO.inserir(confirmacao);
        return paraResponse(confirmacao);
    }

    public ConfirmacaoResponse buscar(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    /** Só o comentário muda. Quem confirmou e qual ocorrência continuam os mesmos. */
    public ConfirmacaoResponse atualizar(Long id, ConfirmacaoRequest request) {
        Confirmacao confirmacao = buscarEntidade(id);
        if (!confirmacao.getMorador().getId().equals(request.getMoradorId())) {
            throw new ConfirmacaoInvalidaException("Só o próprio morador pode editar a confirmação "
                    + "(moradorId deve ser " + confirmacao.getMorador().getId() + ").");
        }
        confirmacao.setComentario(request.getComentario());
        confirmacaoDAO.atualizar(confirmacao);
        return paraResponse(confirmacao);
    }

    public void remover(Long id) {
        buscarEntidade(id);
        confirmacaoDAO.remover(id);
    }

    /** Converte o model em Response. O id e o nome do morador são preenchidos à mão. */
    private ConfirmacaoResponse paraResponse(Confirmacao confirmacao) {
        ConfirmacaoResponse response = mapper.map(confirmacao, ConfirmacaoResponse.class);
        response.setMoradorId(confirmacao.getMorador().getId());
        response.setMoradorNome(confirmacao.getMorador().getNome());
        return response;
    }

    private Confirmacao buscarEntidade(Long id) {
        return confirmacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Confirmação", id));
    }
}
