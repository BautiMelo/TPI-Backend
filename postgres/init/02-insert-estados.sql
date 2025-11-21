-- Insertar estados de contenedor
-- Solo se insertan si no existen (evita duplicados en reinicios)

-- Conectar a la base de datos correcta
\c tpi_backend_db;

-- Estados de Contenedor: LIBRE, ASIGNADO, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
INSERT INTO estados_contenedor (nombre) 
SELECT 'LIBRE' 
WHERE NOT EXISTS (SELECT 1 FROM estados_contenedor WHERE nombre = 'LIBRE');

INSERT INTO estados_contenedor (nombre) 
SELECT 'ASIGNADO' 
WHERE NOT EXISTS (SELECT 1 FROM estados_contenedor WHERE nombre = 'ASIGNADO');

INSERT INTO estados_contenedor (nombre) 
SELECT 'EN_TRANSITO' 
WHERE NOT EXISTS (SELECT 1 FROM estados_contenedor WHERE nombre = 'EN_TRANSITO');

INSERT INTO estados_contenedor (nombre) 
SELECT 'EN_DEPOSITO' 
WHERE NOT EXISTS (SELECT 1 FROM estados_contenedor WHERE nombre = 'EN_DEPOSITO');

INSERT INTO estados_contenedor (nombre) 
SELECT 'ENTREGADO' 
WHERE NOT EXISTS (SELECT 1 FROM estados_contenedor WHERE nombre = 'ENTREGADO');

-- Estados de Solicitud (si es necesario agregar más adelante)
-- Ejemplos: BORRADOR, PENDIENTE, PROGRAMADA, EN_TRANSITO, FINALIZADA, CANCELADA

-- Verificar que se insertaron correctamente
SELECT * FROM estados_contenedor ORDER BY id_estado;
