package ch.bzz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    private final DatabaseConfig config;

    public UserRepository(DatabaseConfig config) {
        this.config = config;
    }

    User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, password_hash, password_salt FROM users WHERE email = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("password_salt"));
            }
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
