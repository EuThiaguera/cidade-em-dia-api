package br.com.fiap.cidadeemdia.controller;

import br.com.fiap.cidadeemdia.dto.LoginRequest;
import br.com.fiap.cidadeemdia.dto.MoradorRequest;
import br.com.fiap.cidadeemdia.dto.MoradorResponse;
import br.com.fiap.cidadeemdia.service.MoradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Rotas REST de /moradores. */
@RestController
@RequestMapping("/moradores")
public class MoradorController {

    private final MoradorService service;

    public MoradorController(MoradorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MoradorResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoradorResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<MoradorResponse> cadastrar(@Valid @RequestBody MoradorRequest request) {
        MoradorResponse criado = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** Login: 200 com os dados do morador ou 401 se e-mail/senha estiverem errados. */
    @PostMapping("/login")
    public ResponseEntity<MoradorResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MoradorResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody MoradorRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
