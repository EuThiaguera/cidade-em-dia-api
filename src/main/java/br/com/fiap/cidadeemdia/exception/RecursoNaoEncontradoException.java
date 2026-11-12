package br.com.fiap.cidadeemdia.exception;

/** O id informado na URL não existe no banco. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado(a): id " + id);
    }
}
