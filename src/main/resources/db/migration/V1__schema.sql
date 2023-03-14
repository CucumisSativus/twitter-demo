DROP TABLE IF EXISTS genre;

CREATE TABLE genre (
                       id   BIGSERIAL    NOT NULL PRIMARY KEY,
                       name  VARCHAR(255) NOT NULL UNIQUE
);