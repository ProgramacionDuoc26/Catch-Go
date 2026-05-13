INSERT INTO categorias_habilidad (nombre) VALUES
    ('Construcción'),
    ('Tecnología'),
    ('Salud'),
    ('Oficios'),
    ('Administración'),
    ('Servicios')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO habilidades (nombre, categoria_id, predeterminada) VALUES
    ('Albañilería',                   1, TRUE),
    ('Soldadura',                     1, TRUE),
    ('Carpintería',                   1, TRUE),
    ('Instalación de tejas',          1, TRUE),
    ('Pintura',                       1, TRUE),
    ('Estuque',                       1, TRUE),
    ('Programación',                  2, TRUE),
    ('Soporte técnico',               2, TRUE),
    ('Redes y telecomunicaciones',    2, TRUE),
    ('Electrónica',                   2, TRUE),
    ('Instalación de equipos',        2, TRUE),
    ('Enfermería básica',             3, TRUE),
    ('Primeros auxilios',             3, TRUE),
    ('Cuidado de adultos mayores',    3, TRUE),
    ('Farmacia',                      3, TRUE),
    ('Gasfitería',                    4, TRUE),
    ('Electricidad',                  4, TRUE),
    ('Mecánica automotriz',           4, TRUE),
    ('Refrigeración y climatización', 4, TRUE),
    ('Contabilidad',                  5, TRUE),
    ('Secretaría',                    5, TRUE),
    ('Recursos humanos',              5, TRUE),
    ('Gestión de inventario',         5, TRUE),
    ('Aseo y limpieza',               6, TRUE),
    ('Seguridad y vigilancia',        6, TRUE),
    ('Delivery y transporte',         6, TRUE),
    ('Cocina y gastronomía',          6, TRUE),
    ('Atención al cliente',           6, TRUE)
ON CONFLICT (nombre, categoria_id) DO NOTHING;
