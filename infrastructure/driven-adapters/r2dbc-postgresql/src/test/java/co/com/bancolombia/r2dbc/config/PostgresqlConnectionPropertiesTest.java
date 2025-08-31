package co.com.bancolombia.r2dbc.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresqlConnectionPropertiesTest {

    @Test
    void shouldCreatePostgresqlConnectionPropertiesSuccessfully() {
        // Arrange
        String host = "localhost";
        Integer port = 5432;
        String database = "my-database";
        String schema = "public";
        String username = "user";
        String password = "password";

        // Act
        PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                host,
                port,
                database,
                schema,
                username,
                password
        );

        // Assert
        assertEquals(host, properties.host());
        assertEquals(port, properties.port());
        assertEquals(database, properties.database());
        assertEquals(schema, properties.schema());
        assertEquals(username, properties.username());
        assertEquals(password, properties.password());
    }
}