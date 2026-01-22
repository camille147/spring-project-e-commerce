DELETE FROM users;

INSERT INTO users (email, first_name, last_name, password, role, birth_date, created_at)
VALUES ('admin@techzone.com', 'Admin', 'Chief', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ADMIN', '1990-01-01', CURRENT_TIMESTAMP);

INSERT INTO users (email, first_name, last_name, password, role, birth_date, created_at)
VALUES ('user@techzone.com', 'Camille', 'P', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'USER', '1995-01-01', CURRENT_TIMESTAMP);