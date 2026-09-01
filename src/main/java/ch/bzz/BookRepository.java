package ch.bzz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    private static final int BATCH_SIZE = 500;

    private final DatabaseConfig config;

    public BookRepository(DatabaseConfig config) {
        this.config = config;
    }

    List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, isbn, title, author, publication_year FROM books ORDER BY id";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                books.add(new Book(
                        resultSet.getInt("id"),
                        resultSet.getString("isbn"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("publication_year")));
            }
        }
        return books;
    }

    int insertAll(List<Book> books) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publication_year) VALUES (?, ?, ?, ?)";

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int inserted = 0;
                int pendingInBatch = 0;
                for (Book book : books) {
                    statement.setString(1, book.isbn());
                    statement.setString(2, book.title());
                    statement.setString(3, book.author());
                    if (book.publicationYear() != null) {
                        statement.setInt(4, book.publicationYear());
                    } else {
                        statement.setNull(4, Types.INTEGER);
                    }
                    statement.addBatch();
                    inserted++;
                    pendingInBatch++;

                    if (pendingInBatch == BATCH_SIZE) {
                        statement.executeBatch();
                        pendingInBatch = 0;
                    }
                }
                if (pendingInBatch > 0) {
                    statement.executeBatch();
                }
                connection.commit();
                return inserted;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    int deleteAll() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            String deleteSql = "DELETE FROM books";
            int deleted = statement.executeUpdate(deleteSql);
            
            String resetSeqSql = "ALTER SEQUENCE books_id_seq RESTART WITH 1";
            statement.executeUpdate(resetSeqSql);
            
            return deleted;
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
