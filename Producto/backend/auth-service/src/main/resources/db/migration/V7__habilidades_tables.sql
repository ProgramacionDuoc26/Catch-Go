CREATE TABLE IF NOT EXISTS categorias_habilidad (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS habilidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    categoria_id BIGINT NOT NULL REFERENCES categorias_habilidad(id),
    predeterminada BOOLEAN DEFAULT FALSE,
    creador_usuario_id BIGINT REFERENCES user_accounts(id),
    UNIQUE(nombre, categoria_id)
);

CREATE TABLE IF NOT EXISTS habilidades_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES user_accounts(id),
    habilidad_id BIGINT NOT NULL REFERENCES habilidades(id),
    puntos INTEGER DEFAULT 0 CHECK (puntos >= 0 AND puntos <= 100),
    trabajos_aplicados INTEGER DEFAULT 0,
    UNIQUE(usuario_id, habilidad_id)
);
