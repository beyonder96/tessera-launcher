-- Schema do banco D1 (SQLite) para licenças do Tessera Launcher
CREATE TABLE IF NOT EXISTS licenses (
    payment_id TEXT PRIMARY KEY,
    external_reference TEXT UNIQUE,
    email TEXT NOT NULL,
    plan_id TEXT NOT NULL,
    method TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    license_token TEXT,
    activated_at INTEGER NOT NULL DEFAULT 0,
    expires_at INTEGER,
    updated_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_licenses_email_status
    ON licenses (email, status);

CREATE INDEX IF NOT EXISTS idx_licenses_external_ref
    ON licenses (external_reference);
