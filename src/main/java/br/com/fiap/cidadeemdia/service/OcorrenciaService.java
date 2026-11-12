package br.com.fiap.cidadeemdia.service;

import br.com.fiap.cidadeemdia.dao.AtualizacaoDAO;
import br.com.fiap.cidadeemdia.dao.CategoriaDAO;
import br.com.fiap.cidadeemdia.dao.ConfirmacaoDAO;
import br.com.fiap.cidadeemdia.dao.MoradorDAO;
import br.com.fiap.cidadeemdia.dao.OcorrenciaDAO;
import br.com.fiap.cidadeemdia.dto.OcorrenciaRequest;
import br.com.fiap.cidadeemdia.dto.OcorrenciaResponse;
import br.com.fiap.cidadeemdia.exception.OcorrenciaDuplicadaException;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.RegistroEmUsoException;
import br.com.fiap.cidadeemdia.exception.RegraNegocioException;
import br.com.fiap.cidadeemdia.exception.ValidacaoException;
import br.com.fiap.cidadeemdia.model.Categoria;
import br.com.fiap.cidadeemdia.model.Morador;
import br.com.fiap.cidadeemdia.model.Ocorrencia;
import br.com.fiap.cidadeemdia.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Regras da Ocorrência. A principal é a de DUPLICIDADE:
 * não se registra uma ocorrência se já existe outra ABERTA, da MESMA categoria,
 * a até RAIO_DUPLICIDADE_METROS do ponto informado. Nesse caso a API responde 409
 * com o id da existente, e o morador deve confirmá-la.
 */
@Service
public class OcorrenciaService {

    /** Distância máxima para considerar que dois registros são o mesmo problema. */
    public static final double RAIO_DUPLICIDADE_METROS = 50;

    private final OcorrenciaDAO ocorrenciaDAO;
    private final CategoriaDAO categoriaDAO;
    private final MoradorDAO moradorDAO;
    private final AtualizacaoDAO atualizacaoDAO;
    private final ConfirmacaoDAO confirmacaoDAO;
    private final ModelMapper mapper;

    public OcorrenciaService(OcorrenciaDAO ocorrenciaDAO, CategoriaDAO categoriaDAO, MoradorDAO moradorDAO,
                             AtualizacaoDAO atualizacaoDAO, ConfirmacaoDAO confirmacaoDAO, ModelMapper mapper) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.categoriaDAO = categoriaDAO;
        this.moradorDAO = moradorDAO;
        this.atualizacaoDAO = atualizacaoDAO;
        this.confirmacaoDAO = confirmacaoDAO;
        this.mapper = mapper;
    }

    public List<OcorrenciaResponse> listar(StatusOcorrencia status, Long categoriaId) {
        return ocorrenciaDAO.listar(status, categoriaId).stream()
                .map(this::paraResponse)
                .toList();
    }

    public OcorrenciaResponse buscar(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    public OcorrenciaResponse cadastrar(OcorrenciaRequest request) {
        Ocorrencia ocorrencia = mapper.map(request, Ocorrencia.class);
        ocorrencia.setCategoria(validarCategoria(request.getCategoriaId()));
        ocorrencia.setMorador(validarMorador(request.getMoradorId()));

        verificarDuplicidade(ocorrencia, null);

        ocorrencia.setStatus(StatusOcorrencia.REGISTRADA);
        ocorrencia.setDataAbertura(LocalDateTime.now());
        ocorrenciaDAO.inserir(ocorrencia);
        return paraResponse(ocorrencia);
    }

    public OcorrenciaResponse atualizar(Long id, OcorrenciaRequest request) {
        Ocorrencia existente = buscarEntidade(id);
        if (!existente.estaAberta()) {
            throw new RegraNegocioException(
                    "A ocorrência está " + existente.getStatus() + " e não pode mais ser editada.");
        }

        // Copia só os dados editáveis. Autor, status e data de abertura continuam os mesmos.
        existente.setTitulo(request.getTitulo());
        existente.setDescricao(request.getDescricao());
        existente.setLatitude(request.getLatitude());
        existente.setLongitude(request.getLongitude());
        existente.setEndereco(request.getEndereco());
        existente.setUrlFoto(request.getUrlFoto());
        existente.setCategoria(validarCategoria(request.getCategoriaId()));

        verificarDuplicidade(existente, id);

        ocorrenciaDAO.atualizar(existente);
        return paraResponse(existente);
    }

    public void remover(Long id) {
        buscarEntidade(id);
        int atualizacoes = atualizacaoDAO.contarPorOcorrencia(id);
        int confirmacoes = confirmacaoDAO.contarPorOcorrencia(id);
        if (atualizacoes > 0 || confirmacoes > 0) {
            throw new RegistroEmUsoException("A ocorrência possui " + atualizacoes + " atualização(ões) e "
                    + confirmacoes + " confirmação(ões) e não pode ser removida. Use o status DESCARTADA.");
        }
        ocorrenciaDAO.remover(id);
    }

    /** Usado também por AtualizacaoService e ConfirmacaoService. */
    Ocorrencia buscarEntidade(Long id) {
        return ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência", id));
    }

    /**
     * REGRA DE NEGÓCIO: impede ocorrência duplicada.
     * 1) o DAO traz as candidatas num "quadrado" em volta do ponto (filtro rápido no banco);
     * 2) aqui calculamos a distância real (Haversine) e pegamos a mais próxima.
     */
    private void verificarDuplicidade(Ocorrencia nova, Long ignorarId) {
        ocorrenciaDAO.buscarAbertasProximas(
                        nova.getCategoria().getId(), nova.getLatitude(), nova.getLongitude(),
                        RAIO_DUPLICIDADE_METROS, ignorarId)
                .stream()
                .filter(existente -> existente.distanciaEmMetrosAte(nova.getLatitude(), nova.getLongitude())
                        <= RAIO_DUPLICIDADE_METROS)
                .min(Comparator.comparingDouble(
                        existente -> existente.distanciaEmMetrosAte(nova.getLatitude(), nova.getLongitude())))
                .ifPresent(existente -> {
                    throw new OcorrenciaDuplicadaException(existente.getId(),
                            existente.distanciaEmMetrosAte(nova.getLatitude(), nova.getLongitude()));
                });
    }

    private Categoria validarCategoria(Long categoriaId) {
        return categoriaDAO.buscarPorId(categoriaId)
                .orElseThrow(() -> new ValidacaoException("Categoria informada não existe: id " + categoriaId));
    }

    private Morador validarMorador(Long moradorId) {
        return moradorDAO.buscarPorId(moradorId)
                .orElseThrow(() -> new ValidacaoException("Morador informado não existe: id " + moradorId));
    }

    /** Converte o model em Response. Os campos que vêm de categoria e morador são preenchidos à mão. */
    private OcorrenciaResponse paraResponse(Ocorrencia ocorrencia) {
        OcorrenciaResponse response = mapper.map(ocorrencia, OcorrenciaResponse.class);
        response.setCategoriaId(ocorrencia.getCategoria().getId());
        response.setCategoriaNome(ocorrencia.getCategoria().getNome());
        response.setMoradorId(ocorrencia.getMorador().getId());
        response.setMoradorNome(ocorrencia.getMorador().getNome());
        return response;
    }
}
