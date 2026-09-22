package ch.bzz;

public class User {

    private final Integer id;
    private final String email;
    private final String passwordHash;
    private final String passwordSalt;

    public User(Integer id, String email, String passwordHash, String passwordSalt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
