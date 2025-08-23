CREATE TABLE IF NOT EXISTS recruiting_data.report_system (
                                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                           vacancy_id UUID NOT NULL REFERENCES recruiting_data.vacancy(id) ON DELETE CASCADE,
                                                           user_id UUID NOT NULL,
                                                           reason VARCHAR(50) NOT NULL,
                                                           status VARCHAR(50) NOT NULL DEFAULT 'NEW',
                                                           comment TEXT,
                                                           created_at TIMESTAMP NOT NULL DEFAULT NOW()
);