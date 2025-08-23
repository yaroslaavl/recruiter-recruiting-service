CREATE TABLE IF NOT EXISTS recruiting_data.vacancy (
                                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                       company_id UUID NOT NULL,
                                                       recruiter_id VARCHAR(250) NOT NULL,
                                                       category_id UUID NOT NULL,
                                                       title VARCHAR(255) NOT NULL,
                                                       description TEXT NOT NULL,
                                                       requirements_must_have TEXT,
                                                       requirements_nice_to_have TEXT,
                                                       contract_type VARCHAR(50),
                                                       work_mode VARCHAR(50),
                                                       position_level VARCHAR(50),
                                                       workload VARCHAR(50),
                                                       location VARCHAR(255),
                                                       salary_from INT,
                                                       salary_to INT,
                                                       status VARCHAR(50) NOT NULL DEFAULT 'DISABLED',
                                                       is_waiting_for_approval BOOLEAN NOT NULL DEFAULT FALSE,
                                                       last_status_changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                                       created_at TIMESTAMP          NOT NULL DEFAULT NOW(),
                                                       updated_at TIMESTAMP          NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS recruiting_data.application (
                                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                       vacancy_id UUID NOT NULL REFERENCES recruiting_data.vacancy(id) ON DELETE CASCADE,
                                                       candidate_id VARCHAR(250) NOT NULL,
                                                       cv_id UUID NOT NULL,
                                                       status VARCHAR(50) NOT NULL DEFAULT 'NEW',
                                                       cover_letter TEXT,
                                                       applied_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS recruiting_data.application_history (
                                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                       application_id UUID NOT NULL REFERENCES recruiting_data.application(id) ON DELETE CASCADE,
                                                       old_status VARCHAR(50) NOT NULL,
                                                       new_status VARCHAR(50) NOT NULL DEFAULT 'NEW',
                                                       changed_by UUID NOT NULL,
                                                       changed_at TIMESTAMP NOT NULL DEFAULT NOW()
)