-- liquibase formatted sql

-- changeset magofrays:init-notification
CREATE TABLE IF NOT EXISTS notifications (
     id UUID,
     from String NOT NULL,
     recipient UUID NOT NULL,
     message String NOT NULL,
     createdAt DateTime NOT NULL
)
ENGINE = MergeTree()
ORDER BY (recipient, createdAt, id);

ALTER TABLE notifications MODIFY TTL createdAt + INTERVAL 90 DAY;
