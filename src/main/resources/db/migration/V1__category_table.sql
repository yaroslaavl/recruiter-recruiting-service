CREATE TABLE IF NOT EXISTS recruiting_data.category(
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(50) NOT NULL UNIQUE,
                          description TEXT,
                          created_at TIMESTAMP          NOT NULL DEFAULT NOW()
);
