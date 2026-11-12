package br.com.fiap.cidadeemdia.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raiz da API (GET /). Mostra que a API está no ar e lista os recursos.
 * Útil para o professor abrir o link do Render e ver que funciona.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> inicio() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Cidade em Dia");
        info.put("descricao", "Inteligência urbana e participação cidadã - Global Solution 2026/2");
        info.put("status", "online");
        info.put("recursos", List.of(
                "/categorias",
                "/moradores",
                "/ocorrencias",
                "/ocorrencias/{id}/atualizacoes",
                "/ocorrencias/{id}/confirmacoes",
                "/atualizacoes/{id}",
                "/confirmacoes/{id}"));
        return info;
    }
}
