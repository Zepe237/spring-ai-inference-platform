CREATE TABLE submission (
    id            UUID PRIMARY KEY,
    task          VARCHAR(16)  NOT NULL,
    text          TEXT         NOT NULL,
    score         DOUBLE PRECISION NOT NULL,
    band          VARCHAR(4)   NOT NULL,
    feedback      JSONB        NOT NULL,
    features      JSONB        NOT NULL,
    model_version VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_submission_created_at ON submission (created_at DESC);
