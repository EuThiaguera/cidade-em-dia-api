package br.com.fiap.cidadeemdia.model;

import java.util.Set;

/**
 * Ciclo de vida de uma ocorrência.
 *
 * Cada status sabe para quais outros ele pode ir. Assim a regra fica no próprio domínio,
 * e não espalhada em vários "if" pelo código.
 *
 *   REGISTRADA  -> EM_ANALISE ou DESCARTADA
 *   EM_ANALISE  -> EM_EXECUCAO ou DESCARTADA
 *   EM_EXECUCAO -> CONCLUIDA
 *   CONCLUIDA   -> (fim)
 *   DESCARTADA  -> (fim)
 */
public enum StatusOcorrencia {

    REGISTRADA,
    EM_ANALISE,
    EM_EXECUCAO,
    CONCLUIDA,
    DESCARTADA;

    /** Status para os quais este status pode avançar. */
    public Set<StatusOcorrencia> proximosPermitidos() {
        return switch (this) {
            case REGISTRADA -> Set.of(EM_ANALISE, DESCARTADA);
            case EM_ANALISE -> Set.of(EM_EXECUCAO, DESCARTADA);
            case EM_EXECUCAO -> Set.of(CONCLUIDA);
            case CONCLUIDA, DESCARTADA -> Set.of();
        };
    }

    public boolean podeMudarPara(StatusOcorrencia novo) {
        return proximosPermitidos().contains(novo);
    }

    /** Ocorrência encerrada não recebe mais confirmações nem edições. */
    public boolean isEncerrado() {
        return this == CONCLUIDA || this == DESCARTADA;
    }
}
