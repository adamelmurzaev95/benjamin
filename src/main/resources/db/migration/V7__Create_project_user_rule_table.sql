CREATE TYPE project_role_enum AS ENUM (
    'USER',
    'ADMIN',
    'OWNER'
);

CREATE TABLE project_username_role(
    record_id SERIAL NOT NULL PRIMARY KEY,
    project_id INT REFERENCES projects(id),
    username VARCHAR(128),
    role project_role_enum NOT NULL
);