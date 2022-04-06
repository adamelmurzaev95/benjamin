CREATE TYPE task_status as ENUM (
    'NEW',
    'ESTIMATION',
    'IN_PROGRESS',
    'BLOCKED',
    'ACCEPTANCE',
    'DONE'
);

ALTER TABLE tasks
ALTER COLUMN status SET DATA TYPE task_status USING status::task_status,
ADD COLUMN project_id INT4 NOT NULL REFERENCES projects (id);