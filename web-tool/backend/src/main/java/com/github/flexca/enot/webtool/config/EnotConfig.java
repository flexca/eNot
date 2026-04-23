package com.github.flexca.enot.webtool.config;

import com.github.flexca.enot.core.Enot;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

@Configuration
public class EnotConfig {

    @Bean
    public Enot enot(EnotRegistry enotRegistry, @Qualifier("jsonObjectMapper") ObjectMapper jsonObjectMapper,
                     @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        return new Enot.Builder()
                .withRegistry(enotRegistry)
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .build();
    }

    @Bean
    public EnotRegistry enotRegistry() {
         return new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification())
                .withTypeSpecifications(new Asn1TypeSpecification())
                .build();
    }

    @Bean
    @Qualifier("jsonObjectMapper")
    public ObjectMapper jsonObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Qualifier("yamlObjectMapper")
    public ObjectMapper yamlObjectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
