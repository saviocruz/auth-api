package br.lar.auth.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuração de DataSource para ambiente de desenvolvimento local.
 * Ativa quando a propriedade 'datasource.use-env-vars' está definida como true.
 */
@Configuration
@ConditionalOnProperty(name = "datasource.use-env-vars", havingValue = "true")
public class LocalDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Obter credenciais das variáveis de ambiente (definidas no docker-compose.yml)
        String jdbcUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("DATABASE_USER");
        String password = System.getenv("DATABASE_PASSWORD");

        // Valores padrão para desenvolvimento local
        if (jdbcUrl == null) {
            jdbcUrl = "jdbc:postgresql://postgres:5432/auth_db_dev";
        }
        if (username == null) {
            username = "postgres";
        }
        if (password == null) {
            password = "postgres";
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Configurações HikariCP
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        System.out.println("[LocalDataSourceConfig] ✅ DataSource criado a partir de variáveis de ambiente");
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("Username: " + username);

        return dataSource;
    }
}
