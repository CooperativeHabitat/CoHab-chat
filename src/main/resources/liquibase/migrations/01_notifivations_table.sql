-- liquibase formatted sql

-- changeset magofrays:init-notification
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS notifications (
    id UUID NOT NULL,
    sender VARCHAR(255) NOT NULL,
    recipient UUID NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created_at
    ON notifications (recipient, created_at DESC, id);

SELECT create_hypertable('notifications', 'created_at', if_not_exists => TRUE);

SELECT add_retention_policy('notifications', INTERVAL '2 years', if_not_exists => TRUE);

ALTER TABLE notifications SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'recipient',
    timescaledb.compress_orderby = 'created_at DESC, id'
);

SELECT add_compression_policy('notifications', INTERVAL '7 days', if_not_exists => TRUE);