package br.com.fiap.cidadeemdia.controller;

import br.com.fiap.cidadeemdia.dto.AtualizacaoRequest;
import br.com.fiap.cidadeemdia.dto.AtualizacaoResponse;
import br.com.fiap.cidadeemdia.service.AtualizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas de uma atualização específica. Cadastrar e listar ficam aninhados em
 * /ocorrencias/{id}/atualizacoes (OcorrenciaController), pois uma atualização sempre pertence a uma ocorrência.
 */
@RestController
@RequestMapping("/atualizacoes")
public class AtualizacaoController {

    private final AtualizacaoService service;

    public AtualizacaoController(AtualizacaoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtualizacaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtualizacaoResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody AtualizacaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
