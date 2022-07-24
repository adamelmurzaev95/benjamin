CREATE TABLE invitations(
    id SERIAL NOT NULL PRIMARY KEY,
    sender VARCHAR(128) NOT NULL,
    receiver VARCHAR(128) NOT NULL,
    project_id INT NOT NULL REFERENCES projects(id),
    role project_role_enum NOT NULL,
    invitation_uuid UUID NOT NULL
);

CREATE TABLE invitations_outbox(
    event_id SERIAL NOT NULL PRIMARY KEY,
    receiver_email VARCHAR(256) NOT NULL,
    message_topic VARCHAR(128) NOT NULL,
    message TEXT
);