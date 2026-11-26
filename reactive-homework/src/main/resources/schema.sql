CREATE TABLE IF NOT EXISTS sports (
    id   SERIAL PRIMARY KEY,
    name TEXT   NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS sports_name_lower_key ON sports (LOWER(name));
