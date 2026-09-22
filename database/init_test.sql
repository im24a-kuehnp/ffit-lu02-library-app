INSERT INTO books (isbn, title, author, publication_year) VALUES
  ('978-0134685991', 'Effective Java', 'Joshua Bloch', 2018),
  ('978-0596009205', 'Head First Java', 'Kathy Sierra, Bert Bates', 2005);

-- Test user with password "geheim123" (salt/hash generated via PasswordHandler, SHA-256)
INSERT INTO users (email, password_hash, password_salt) VALUES
  ('max.mustermann@example.com', 'WgcYCsK4Glti2YMy8gLP/UV3Z/+xSSAvz4Ohs1DykMM=', 'AAECAwQFBgcICQoLDA0ODw==');
