-- ======================================================================
-- PROYECTO: CATCH AND GO
-- BASE DE DATOS: PostgreSQL / Oracle SQL Developer
-- MÓDULOS CUBIERTOS: Auth, Profile, Jobs, Matching, Ranking
-- ======================================================================

-- 1. MÓDULO AUTH & PROFILE SERVICE [cite: 405]
-- Perfil de datos de trabajador y empresa (unificados)

CREATE TABLE USUARIO (
    id_usuario NUMBER GENERATED ALWAYS AS IDENTITY,
    rut VARCHAR2(15) NOT NULL,
    correo VARCHAR2(100) NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    tipo_usuario VARCHAR2(15) NOT NULL, -- 'TRABAJADOR' o 'EMPRESA'
    estado_validacion VARCHAR2(20) DEFAULT 'PENDIENTE', -- Validación de identidad [cite: 385]
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_rut UNIQUE (rut),
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT chk_tipo_usuario CHECK (tipo_usuario IN ('TRABAJADOR', 'EMPRESA'))
);

CREATE TABLE PERFIL_TRABAJADOR (
    id_trabajador NUMBER,
    nombre_completo VARCHAR2(150) NOT NULL,
    especialidad_principal VARCHAR2(100) NOT NULL,
    tarifa_hora_ref NUMBER,
    latitud_actual NUMBER(10,6), -- Para la geolocalización de Mapbox [cite: 413]
    longitud_actual NUMBER(10,6),
    ranking_promedio NUMBER(3,2) DEFAULT 0.00,
    trabajos_completados NUMBER DEFAULT 0,
    CONSTRAINT pk_perfil_trabajador PRIMARY KEY (id_trabajador),
    CONSTRAINT fk_perfil_trab_usuario FOREIGN KEY (id_trabajador) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE
);

CREATE TABLE PERFIL_EMPRESA (
    id_empresa NUMBER,
    razon_social VARCHAR2(150) NOT NULL,
    direccion_comercial VARCHAR2(250),
    ranking_promedio NUMBER(3,2) DEFAULT 0.00,
    contrataciones_realizadas NUMBER DEFAULT 0,
    CONSTRAINT pk_perfil_empresa PRIMARY KEY (id_empresa),
    CONSTRAINT fk_perfil_emp_usuario FOREIGN KEY (id_empresa) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE
);

-- 2. MÓDULO JOBS SERVICE [cite: 405]
-- Publicación de ofertas de empleo temporal con parámetros georreferenciados [cite: 385]

CREATE TABLE OFERTA_EMPLEO (
    id_oferta NUMBER GENERATED ALWAYS AS IDENTITY,
    id_empresa NUMBER NOT NULL,
    titulo_rol VARCHAR2(100) NOT NULL,
    descripcion_tarea VARCHAR2(500),
    tarifa_ofrecida NUMBER NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    latitud_oferta NUMBER(10,6) NOT NULL,
    longitud_oferta NUMBER(10,6) NOT NULL,
    estado_oferta VARCHAR2(20) DEFAULT 'ACTIVA', -- 'ACTIVA', 'ASIGNADA', 'COMPLETADA', 'CANCELADA'
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_oferta_empleo PRIMARY KEY (id_oferta),
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES PERFIL_EMPRESA(id_empresa)
);

-- 3. MÓDULO MATCHING SERVICE [cite: 405]
-- Sistema de postulación y filtrado por proximidad y ranking [cite: 385]

CREATE TABLE POSTULACION (
    id_postulacion NUMBER GENERATED ALWAYS AS IDENTITY,
    id_oferta NUMBER NOT NULL,
    id_trabajador NUMBER NOT NULL,
    distancia_km NUMBER(6,2), -- Calculado por GeoLocation Service al postular
    estado_postulacion VARCHAR2(20) DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'ACEPTADA', 'RECHAZADA'
    fecha_postulacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_postulacion PRIMARY KEY (id_postulacion),
    CONSTRAINT fk_postulacion_oferta FOREIGN KEY (id_oferta) REFERENCES OFERTA_EMPLEO(id_oferta),
    CONSTRAINT fk_postulacion_trabajador FOREIGN KEY (id_trabajador) REFERENCES PERFIL_TRABAJADOR(id_trabajador),
    CONSTRAINT uk_postulacion_unica UNIQUE (id_oferta, id_trabajador)
);

-- 4. MÓDULO RANKING SERVICE [cite: 405]
-- Sistema de evaluación post-trabajo bidireccional (1-5 estrellas + comentario) [cite: 394]

CREATE TABLE EVALUACION (
    id_evaluacion NUMBER GENERATED ALWAYS AS IDENTITY,
    id_postulacion NUMBER NOT NULL,
    id_evaluador NUMBER NOT NULL, -- Usuario que emite la reseña
    id_evaluado NUMBER NOT NULL,  -- Usuario que recibe la reseña
    puntuacion NUMBER(1) NOT NULL, -- 1 a 5 estrellas
    comentario VARCHAR2(500),
    rol_evaluador VARCHAR2(15) NOT NULL, -- 'EMPRESA' o 'TRABAJADOR'
    fecha_evaluacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_evaluacion PRIMARY KEY (id_evaluacion),
    CONSTRAINT fk_eval_postulacion FOREIGN KEY (id_postulacion) REFERENCES POSTULACION(id_postulacion),
    CONSTRAINT fk_eval_evaluador FOREIGN KEY (id_evaluador) REFERENCES USUARIO(id_usuario),
    CONSTRAINT fk_eval_evaluado FOREIGN KEY (id_evaluado) REFERENCES USUARIO(id_usuario),
    CONSTRAINT chk_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT chk_rol_evaluador CHECK (rol_evaluador IN ('TRABAJADOR', 'EMPRESA'))
);

CREATE TABLE PAGO (
    id_pago NUMBER GENERATED ALWAYS AS IDENTITY,
    id_postulacion NUMBER NOT NULL,
    monto_total NUMBER(10,2) NOT NULL,
    comision_plataforma NUMBER(10,2) DEFAULT 0, -- Proyección de ingresos
    estado_pago VARCHAR2(20) DEFAULT 'PENDIENTE', -- PENDIENTE, PAGADO, REEMBOLSADO
    metodo_pago VARCHAR2(30), -- EFECTIVO, TRANSFERENCIA, TARJETA (Futuro)
    fecha_pago TIMESTAMP,
    CONSTRAINT pk_pago PRIMARY KEY (id_pago),
    CONSTRAINT fk_pago_postulacion FOREIGN KEY (id_postulacion) REFERENCES POSTULACION(id_postulacion)
);