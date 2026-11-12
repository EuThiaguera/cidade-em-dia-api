package br.com.fiap.cidadeemdia.service;

import br.com.fiap.cidadeemdia.dao.ConfirmacaoDAO;
import br.com.fiap.cidadeemdia.dao.MoradorDAO;
import br.com.fiap.cidadeemdia.dao.OcorrenciaDAO;
import br.com.fiap.cidadeemdia.dto.LoginRequest;
import br.com.fiap.cidadeemdia.dto.MoradorRequest;
import br.com.fiap.cidadeemdia.dto.MoradorResponse;
import br.com.fiap.cidadeemdia.exception.DadoDuplicadoException;
import br.com.fiap.cidadeemdia.exception.LoginInvalidoException;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.RegistroEmUsoException;
import br.com.fiap.cidadeemdia.model.Morador;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Regras de Morador: e-mail único e não remover quem já participou (ocorrências/confirmações). */
@Service
public class MoradorService {

    private final MoradorDAO moradorDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final ConfirmacaoDAO confirmacaoDAO;
    private final ModelMapper mapper;

    public MoradorService(MoradorDAO moradorDAO, OcorrenciaDAO ocorrenciaDAO,
                          ConfirmacaoDAO confirmacaoDAO, ModelMapper mapper) {
        this.moradorDAO = moradorDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.confirmacaoDAO = confirmacaoDAO;
        this.mapper = mapper;
    }

    public List<MoradorResponse> listar() {
        return moradorDAO.listar().stream()
                .map(m -> mapper.map(m, MoradorResponse.class))
                .toList();
    }

    public MoradorResponse buscar(Long id) {
        return mapper.map(buscarEntidade(id), MoradorResponse.class);
    }

    public MoradorResponse cadastrar(MoradorRequest request) {
        Morador morador = mapper.map(request, Morador.class);
        morador.setEmail(morador.getEmail().trim().toLowerCase());
        morador.setCpf(somenteNumeros(morador.getCpf()));
        validarEmailUnico(morador.getEmail(), null);
        validarCpfUnico(morador.getCpf(), null);
        morador.setDataCadastro(LocalDateTime.now());
        return mapper.map(moradorDAO.inserir(morador), MoradorResponse.class);
    }

    public MoradorResponse atualizar(Long id, MoradorRequest request) {
        Morador existente = buscarEntidade(id);
        Morador morador = mapper.map(request, Morador.class);
        morador.setId(id);
        morador.setEmail(morador.getEmail().trim().toLowerCase());
        morador.setCpf(somenteNumeros(morador.getCpf()));
        morador.setDataCadastro(existente.getDataCadastro());
        validarEmailUnico(morador.getEmail(), id);
        validarCpfUnico(morador.getCpf(), id);
        moradorDAO.atualizar(morador);
        return mapper.map(morador, MoradorResponse.class);
    }

    public void remover(Long id) {
        buscarEntidade(id);
        int ocorrencias = ocorrenciaDAO.contarPorMorador(id);
        int confirmacoes = confirmacaoDAO.contarPorMorador(id);
        if (ocorrencias > 0 || confirmacoes > 0) {
            throw new RegistroEmUsoException("O morador possui " + ocorrencias + " ocorrência(s) e "
                    + confirmacoes + " confirmação(ões) e não pode ser removido.");
        }
        moradorDAO.remover(id);
    }

    /**
     * Login simples: procura o morador pelo e-mail e compara a senha.
     * A mesma mensagem para "e-mail não existe" e "senha errada" (não revela qual dos dois errou).
     */
    public MoradorResponse login(LoginRequest request) {
        Morador morador = moradorDAO.buscarPorEmail(request.getEmail().trim())
                .orElseThrow(LoginInvalidoException::new);

        if (!morador.getSenha().equals(request.getSenha())) {
            throw new LoginInvalidoException();
        }
        return mapper.map(morador, MoradorResponse.class);
    }

    /** Busca o morador ou lança 404. */
    private Morador buscarEntidade(Long id) {
        return moradorDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Morador", id));
    }

    private void validarCpfUnico(String cpf, Long ignorarId) {
        if (moradorDAO.existeCpf(cpf, ignorarId)) {
            throw new DadoDuplicadoException("O CPF " + cpf + " já está cadastrado.");
        }
    }

    /** "123.456.789-09" -> "12345678909" (o banco guarda só os números). */
    private String somenteNumeros(String texto) {
        return texto.replaceAll("\\D", "");
    }

    private void validarEmailUnico(String email, Long ignorarId) {
        if (moradorDAO.existeEmail(email, ignorarId)) {
            throw new DadoDuplicadoException("O e-mail " + email + " já está cadastrado.");
        }
    }
}
