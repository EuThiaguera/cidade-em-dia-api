package br.com.fiap.cidadeemdia.controller;

import br.com.fiap.cidadeemdia.dto.ConfirmacaoRequest;
import br.com.fiap.cidadeemdia.dto.ConfirmacaoResponse;
import br.com.fiap.cidadeemdia.service.ConfirmacaoService;
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
 * Rotas de uma confirmação específica. Confirmar e listar ficam aninhados em
 * /ocorrencias/{id}/confirmacoes (OcorrenciaController).
 */
@RestController
@RequestMapping("/confirmacoes")
public class ConfirmacaoController {

    private final ConfirmacaoService service;

    public ConfirmacaoController(ConfirmacaoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfirmacaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfirmacaoResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ConfirmacaoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    /** "Desfazer" a confirmação. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
