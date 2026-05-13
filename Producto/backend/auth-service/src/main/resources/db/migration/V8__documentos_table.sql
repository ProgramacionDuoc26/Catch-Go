CREATE TABLE IF NOT EXISTS documentos_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES user_accounts(id),
    tipo VARCHAR(100) NOT NULL,
    filename VARCHAR(500) NOT NULL,
    fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
