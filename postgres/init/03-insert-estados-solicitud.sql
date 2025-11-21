-- Estados de Solicitud: BORRADOR, PENDIENTE, PROGRAMADA, EN_TRANSITO, COMPLETADA, CANCELADA
-- Este script inserta o actualiza los estados de solicitud en la base de datos

-- Conectar a la base de datos correcta
\c tpi_backend_db;

-- Limpiar estados antiguos si existen (EN_PROCESO)
DELETE FROM estado_solicitud WHERE nombre = 'EN_PROCESO';

-- Insertar estados oficiales de solicitud (solo si no existen)
INSERT INTO estado_solicitud (nombre) 
SELECT 'BORRADOR' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'BORRADOR');

INSERT INTO estado_solicitud (nombre) 
SELECT 'PENDIENTE' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'PENDIENTE');

INSERT INTO estado_solicitud (nombre) 
SELECT 'PROGRAMADA' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'PROGRAMADA');

INSERT INTO estado_solicitud (nombre) 
SELECT 'EN_TRANSITO' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'EN_TRANSITO');

INSERT INTO estado_solicitud (nombre) 
SELECT 'COMPLETADA' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'COMPLETADA');

INSERT INTO estado_solicitud (nombre) 
SELECT 'CANCELADA' 
WHERE NOT EXISTS (SELECT 1 FROM estado_solicitud WHERE nombre = 'CANCELADA');

-- Verificar que se insertaron correctamente
SELECT * FROM estado_solicitud ORDER BY id_estado;
