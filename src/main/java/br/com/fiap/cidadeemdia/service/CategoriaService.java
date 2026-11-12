package br.com.fiap.cidadeemdia.service;

import br.com.fiap.cidadeemdia.dao.CategoriaDAO;
import br.com.fiap.cidadeemdia.dao.OcorrenciaDAO;
import br.com.fiap.cidadeemdia.dto.CategoriaRequest;
import br.com.fiap.cidadeemdia.dto.CategoriaResponse;
import br.com.fiap.cidadeemdia.exception.DadoDuplicadoException;
import br.com.fiap.cidadeemdia.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadeemdia.exception.RegistroEmUsoException;
import br.com.fiap.cidadeemdia.model.Categoria;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Regras de Categoria: nome único e não remover categoria que já tem ocorrências. */
@Service
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final ModelMapper mapper;

    public CategoriaService(CategoriaDAO categoriaDAO, OcorrenciaDAO ocorrenciaDAO, ModelMapper mapper) {
        this.categoriaDAO = categoriaDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.mapper = mapper;
    }

    public List<CategoriaResponse> listar() {
        return categoriaDAO.listar().stream()
                .map(c -> mapper.map(c, CategoriaResponse.class))
                .toList();
    }

    public CategoriaResponse buscar(Long id) {
        return mapper.map(buscarEntidade(id), CategoriaResponse.class);
    }

    public CategoriaResponse cadastrar(CategoriaRequest request) {
        Categoria categoria = mapper.map(request, Categoria.class);
        categoria.setNome(categoria.getNome().trim());
        validarNomeUnico(categoria.getNome(), null);
        return mapper.map(categoriaDAO.inserir(categoria), CategoriaResponse.class);
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        buscarEntidade(id);
        Categoria categoria = mapper.map(request, Categoria.class);
        categoria.setId(id);
        categoria.setNome(categoria.getNome().trim());
        validarNomeUnico(categoria.getNome(), id);
        categoriaDAO.atualizar(categoria);
        return mapper.map(categoria, CategoriaResponse.class);
    }

    public void remover(Long id) {
        buscarEntidade(id);
        int ocorrencias = ocorrenciaDAO.contarPorCategoria(id);
        if (ocorrencias > 0) {
            throw new RegistroEmUsoException(
                    "A categoria possui " + ocorrencias + " ocorrência(s) e não pode ser removida.");
        }
        categoriaDAO.remover(id);
    }

    /** Busca a categoria ou lança 404. */
    private Categoria buscarEntidade(Long id) {
        return categoriaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private void validarNomeUnico(String nome, Long ignorarId) {
        if (categoriaDAO.existeNome(nome, ignorarId)) {
            throw new DadoDuplicadoException("Já existe uma categoria com o nome '" + nome + "'.");
        }
    }
}
