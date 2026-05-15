CREATE TABLE IF NOT EXISTS job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    fecha_postulacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job FOREIGN KEY (job_id) REFERENCES job_offers(id)
);
