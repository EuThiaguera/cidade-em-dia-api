package br.com.fiap.cidadeemdia.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cria UM ModelMapper para a aplicação inteira.
 * O @Bean faz o Spring guardar esse objeto e entregar para todo service que pedir no construtor.
 *
 * STRICT: só copia campos com o nome exatamente igual (titulo -> titulo, status -> status).
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
