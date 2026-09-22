package ch.bzz;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;

import ch.bzz.security.JwtHandler;
import ch.bzz.security.PasswordHandler;
import io.javalin.Javalin;

public class JavalinMain {

    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.load("config.properties");
        UserRepository userRepository = new UserRepository(config);

        Javalin app = Javalin.create();
        app.post("/auth/login", ctx -> {
            var json = ctx.bodyValidator(Map.class)
                    .check(m -> m.containsKey("email"), "email is required")
                    .check(m -> m.containsKey("password"), "password is required")
                    .get();

            String inputEmail = (String) json.get("email");
            String inputPassword = (String) json.get("password");

            User user = userRepository.findByEmail(inputEmail);

            if (user != null) {
                byte[] storedSalt = Base64.getDecoder().decode(user.getPasswordSalt());
                byte[] storedHash = Base64.getDecoder().decode(user.getPasswordHash());

                if (PasswordHandler.verifyPassword(inputPassword, storedHash, storedSalt)) {
                    String jwt = JwtHandler.createJwt(inputEmail, user.getId());
                    ctx.json(Map.of("token", jwt));
                    return;
                }
            }

            // Use the same error message if the user is not found and if the password is wrong
            ctx.status(401).json(Map.of("error", "Invalid email or password"));
        });

        app.exception(SQLException.class, (e, ctx) -> ctx.status(500).json(Map.of("error", "Internal server error")));
        app.exception(NoSuchAlgorithmException.class,
                (e, ctx) -> ctx.status(500).json(Map.of("error", "Internal server error")));

        app.start(7070);
    }
}
