package br.lar.auth.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Gerenciador de credenciais do AWS Secrets Manager.
 * Ativa apenas em ambiente de produção quando a propriedade 'datasource.use-env-vars' é false.
 */
@Component
@ConditionalOnProperty(name = "datasource.use-env-vars", havingValue = "false", matchIfMissing = true)
public class AwsSecretsManager {

    private static final String SECRET_NAME = "rds!cluster-cc34707b-6e72-4db4-b697-8ef5f0f2f439";
    private static final Region REGION = Region.US_EAST_1;

    public DatabaseCredentials getCredentials() {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(REGION)
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            String secretString = response.secretString();

            // Parse do JSON retornado
            ObjectMapper mapper = new ObjectMapper();
            JsonNode secretJson = mapper.readTree(secretString);

            return new DatabaseCredentials(
                    secretJson.get("username").asText(),
                    secretJson.get("password").asText(),
                    secretJson.get("host").asText(),
                    secretJson.get("port").asInt(),
                    secretJson.get("dbname").asText()
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao recuperar credenciais do Secrets Manager", e);
        }
    }

    public static class DatabaseCredentials {
        private final String username;
        private final String password;
        private final String host;
        private final int port;
        private final String dbname;

        public DatabaseCredentials(String username, String password, String host, int port, String dbname) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.dbname = dbname;
        }

        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getDbname() { return dbname; }

        public String getJdbcUrl() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbname);
        }
    }
}