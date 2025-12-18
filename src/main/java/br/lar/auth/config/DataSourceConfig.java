package br.lar.auth.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuração de DataSource para ambiente de produção AWS.
 * Ativa quando a propriedade 'datasource.use-env-vars' está definida como false (padrão).
 */
@Configuration
@ConditionalOnProperty(name = "datasource.use-env-vars", havingValue = "false", matchIfMissing = true)
public class DataSourceConfig {

    private final AwsSecretsManager secretsManager;

    public DataSourceConfig(AwsSecretsManager secretsManager) {
        this.secretsManager = secretsManager;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        AwsSecretsManager.DatabaseCredentials credentials = secretsManager.getCredentials();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(credentials.getJdbcUrl());
        dataSource.setUsername(credentials.getUsername());
        dataSource.setPassword(credentials.getPassword());
        dataSource.setDriverClassName("org.postgresql.Driver");

        System.out.println("[DataSourceConfig] ✅ DataSource criado a partir do AWS Secrets Manager");
        System.out.println("JDBC URL: " + credentials.getJdbcUrl());
        System.out.println("Username: " + credentials.getUsername());

        // Configurações recomendadas para Aurora
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }
}