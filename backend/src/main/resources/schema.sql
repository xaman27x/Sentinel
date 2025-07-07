-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    subscription_tier VARCHAR(50) NOT NULL DEFAULT 'FREE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- API Keys table
CREATE TABLE IF NOT EXISTS api_keys (
    api_key_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    key_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used TIMESTAMP,
    usage_count BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Rate Limits table (FIXED: api_key_id changed to UUID)
CREATE TABLE IF NOT EXISTS rate_limits (
    rate_limit_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    api_key_id UUID,  -- Changed from VARCHAR(255) to UUID
    requests_count INTEGER NOT NULL DEFAULT 0,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    last_request TIMESTAMP,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    blocked_until TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (api_key_id) REFERENCES api_keys(api_key_id) ON DELETE CASCADE
);

-- Moderation Logs table (FIXED: api_key_id changed to UUID)
CREATE TABLE IF NOT EXISTS moderation_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    api_key_id UUID,  -- Changed from VARCHAR(255) to UUID
    input_text TEXT,
    is_offensive BOOLEAN NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    detected_terms TEXT,
    processing_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (api_key_id) REFERENCES api_keys(api_key_id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_api_keys_key ON api_keys(api_key);
CREATE INDEX IF NOT EXISTS idx_api_keys_user_id ON api_keys(user_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_user_id ON rate_limits(user_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_api_key_id ON rate_limits(api_key_id);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_user_id ON moderation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_created_at ON moderation_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_is_offensive ON moderation_logs(is_offensive);