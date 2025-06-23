-- Crear la tabla de usuarios si no existe
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(64) NOT NULL,
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL
);

-- Agregar columna dark_mode si no existe (versión compatible con Spring Boot)
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS dark_mode BOOLEAN DEFAULT FALSE;

-- Crear la tabla de roles si no existe
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(64) NOT NULL UNIQUE
);

-- Crear la tabla intermedia para la relación muchos a muchos entre usuarios y roles
CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id BIGINT,
    rol_id BIGINT,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Crear la tabla de favoritos
CREATE TABLE IF NOT EXISTS favoritos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    article_id VARCHAR(255) NOT NULL,
    title TEXT NOT NULL,
    authors TEXT,
    publication_name TEXT,
    publication_date VARCHAR(50),
    doi VARCHAR(255),
    url TEXT,
    abstract TEXT,
    cited_by_count INTEGER DEFAULT 0,
    open_access VARCHAR(10),
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE(usuario_id, article_id)
);

-- Insertar roles en la tabla roles (solo si no existen ya)
INSERT INTO roles (nombre)
SELECT 'ROLE_ADMIN' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'ROLE_ADMIN');

INSERT INTO roles (nombre)
SELECT 'ROLE_USER' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'ROLE_USER');

-- Insertar el usuario admin (solo si no existe)
INSERT INTO usuarios (nombre, email, password, dark_mode)
SELECT 'Antonio', 'antonio@gmail.com', '$2a$12$9Sars17nXq0UYeX8qh54lO8MKi964ejmWefdi/9x4flwOcHPAS4.e', FALSE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'antonio@gmail.com');

-- Asignar el rol ADMIN al usuario Antonio (solo si no tiene ya ese rol)
INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.email = 'antonio@gmail.com' AND r.nombre = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM usuarios_roles ur 
    WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
);