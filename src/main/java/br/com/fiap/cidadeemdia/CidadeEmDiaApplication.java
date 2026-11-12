package br.com.fiap.cidadeemdia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API do Cidade em Dia.
 * O Spring Boot sobe o Tomcat embutido e registra controllers, services, DAOs e configurações.
 */
@SpringBootApplication
public class CidadeEmDiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CidadeEmDiaApplication.class, args);
    }
}
