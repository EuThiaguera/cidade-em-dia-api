package br.com.fiap.cidadeemdia.controller;

import br.com.fiap.cidadeemdia.dto.AtualizacaoRequest;
import br.com.fiap.cidadeemdia.dto.AtualizacaoResponse;
import br.com.fiap.cidadeemdia.dto.ConfirmacaoRequest;
import br.com.fiap.cidadeemdia.dto.ConfirmacaoResponse;
import br.com.fiap.cidadeemdia.dto.OcorrenciaRequest;
import br.com.fiap.cidadeemdia.dto.OcorrenciaResponse;
import br.com.fiap.cidadeemdia.model.StatusOcorrencia;
import br.com.fiap.cidadeemdia.service.AtualizacaoService;
import br.com.fiap.cidadeemdia.service.ConfirmacaoService;
import br.com.fiap.cidadeemdia.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas REST de /ocorrencias, incluindo as rotas ANINHADAS dos relacionamentos 1:N:
 *   /ocorrencias/{id}/atualizacoes  (linha do tempo)
 *   /ocorrencias/{id}/confirmacoes  (apoio dos moradores)
 */
@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;
    private final AtualizacaoService atualizacaoService;
    private final ConfirmacaoService confirmacaoService;

    public OcorrenciaController(OcorrenciaService ocorrenciaService, AtualizacaoService atualizacaoService,
                                ConfirmacaoService confirmacaoService) {
        this.ocorrenciaService = ocorrenciaService;
        this.atualizacaoService = atualizacaoService;
        this.confirmacaoService = confirmacaoService;
    }

    // ---------------------------- CRUD da ocorrência ----------------------------

    /** Filtros opcionais: /ocorrencias?status=REGISTRADA&categoriaId=1 */
    @GetMapping
    public ResponseEntity<List<OcorrenciaResponse>> listar(
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(ocorrenciaService.listar(status, categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ocorrenciaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponse> cadastrar(@Valid @RequestBody OcorrenciaRequest request) {
        OcorrenciaResponse criada = ocorrenciaService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponse> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody OcorrenciaRequest request) {
        return ResponseEntity.ok(ocorrenciaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        ocorrenciaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------- 1:N Atualizações ----------------------------

    @GetMapping("/{id}/atualizacoes")
    public ResponseEntity<List<AtualizacaoResponse>> listarAtualizacoes(@PathVariable Long id) {
        return ResponseEntity.ok(atualizacaoService.listarPorOcorrencia(id));
    }

    @PostMapping("/{id}/atualizacoes")
    public ResponseEntity<AtualizacaoResponse> registrarAtualizacao(@PathVariable Long id,
                                                                    @Valid @RequestBody AtualizacaoRequest request) {
        AtualizacaoResponse criada = atualizacaoService.registrar(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/atualizacoes/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    // ---------------------------- 1:N Confirmações ----------------------------

    @GetMapping("/{id}/confirmacoes")
    public ResponseEntity<List<ConfirmacaoResponse>> listarConfirmacoes(@PathVariable Long id) {
        return ResponseEntity.ok(confirmacaoService.listarPorOcorrencia(id));
    }

    @PostMapping("/{id}/confirmacoes")
    public ResponseEntity<ConfirmacaoResponse> confirmar(@PathVariable Long id,
                                                         @Valid @RequestBody ConfirmacaoRequest request) {
        ConfirmacaoResponse criada = confirmacaoService.confirmar(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/confirmacoes/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }
}
