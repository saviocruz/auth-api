package br.lar.auth.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para escanear os mappers do MapStruct
 */
@Configuration
@ComponentScan(basePackages = "br.lar.auth.mapper")
public class MapperConfig {
}