--liquibase formatted sql

--changeset argenta:2023-03-06-1-create-account-table
CREATE TABLE IF NOT EXISTS Account
(
    id           UUID default random_uuid() PRIMARY KEY,
    version      INTEGER,
    name         VARCHAR(255) NOT NULL,
    balance      DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    balanceLimit DECIMAL(19, 2) NOT NULL DEFAULT 0.00
);
-- rollback drop table Account