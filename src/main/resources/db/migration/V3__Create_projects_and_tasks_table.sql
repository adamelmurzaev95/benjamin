CREATE TABLE projects(
    id SERIAL NOT NULL PRIMARY KEY,
    title VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(512)
);

CREATE TABLE tasks(
    id SERIAL NOT NULL PRIMARY KEY,
    title VARCHAR(64) NOT NULL,
    description VARCHAR(512),
    creation_date_time TIMESTAMP NOT NULL,
    last_modified_date_time TIMESTAMP NOT NULL,
    author VARCHAR(128) NOT NULL,
    assignee VARCHAR(128),
    status VARCHAR(128) NOT NULL
);