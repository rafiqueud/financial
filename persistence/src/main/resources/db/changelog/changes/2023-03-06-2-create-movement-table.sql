--liquibase formatted sql

--changeset argenta:2023-03-06-2-create-movement-table
CREATE TABLE IF NOT EXISTS Movement
(
    id          UUID DEFAULT random_uuid() PRIMARY KEY,
    account_id  UUID,
    description VARCHAR(255),
    amount      DECIMAL(19, 2),
    type        VARCHAR(50),
    date        TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES Account (id)
);
-- rollback drop table MovementEntity