-- Migraciones para soporte de rutas con tramos automáticos
-- Fecha: 2024
-- Descripción: Agrega campos necesarios para el cálculo automático de rutas y tramos

-- 1. Agregar columnas a la tabla tramos
ALTER TABLE tramos ADD COLUMN IF NOT EXISTS orden INTEGER;
ALTER TABLE tramos ADD COLUMN IF NOT EXISTS generado_automaticamente BOOLEAN DEFAULT FALSE;
ALTER TABLE tramos ADD COLUMN IF NOT EXISTS duracion_horas DOUBLE PRECISION;

-- 2. Agregar columna a la tabla rutas
ALTER TABLE rutas ADD COLUMN IF NOT EXISTS modificada_manualmente BOOLEAN DEFAULT FALSE;

-- 3. Comentarios para documentar las columnas
COMMENT ON COLUMN tramos.orden IS 'Orden del tramo en la ruta (1, 2, 3, etc.)';
COMMENT ON COLUMN tramos.generado_automaticamente IS 'Indica si el tramo fue generado automáticamente por cálculo de ruta';
COMMENT ON COLUMN tramos.duracion_horas IS 'Duración estimada del tramo en horas (calculada por OSRM)';
COMMENT ON COLUMN rutas.modificada_manualmente IS 'Indica si la ruta fue modificada manualmente después de la generación automática';

-- 4. Índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_tramos_ruta_orden ON tramos(ruta_id, orden);
CREATE INDEX IF NOT EXISTS idx_tramos_generado_auto ON tramos(generado_automaticamente) WHERE generado_automaticamente = true;

-- 5. Actualizar datos existentes (si es necesario)
-- Marcar todos los tramos existentes como NO generados automáticamente
UPDATE tramos SET generado_automaticamente = FALSE WHERE generado_automaticamente IS NULL;

-- Marcar todas las rutas existentes como NO modificadas manualmente
UPDATE rutas SET modificada_manualmente = FALSE WHERE modificada_manualmente IS NULL;

-- 6. Verificación
SELECT 
    'Tramos con orden NULL' as verificacion,
    COUNT(*) as cantidad
FROM tramos 
WHERE orden IS NULL
UNION ALL
SELECT 
    'Tramos generados automáticamente' as verificacion,
    COUNT(*) as cantidad
FROM tramos 
WHERE generado_automaticamente = TRUE
UNION ALL
SELECT 
    'Rutas modificadas manualmente' as verificacion,
    COUNT(*) as cantidad
FROM rutas 
WHERE modificada_manualmente = TRUE;
