-- =====================================================
-- SCRIPT DE DATOS DE PRUEBA REALISTAS - TPI BACKEND
-- =====================================================

-- Reiniciar secuencias de IDs para asegurar que empiecen desde 1
ALTER SEQUENCE IF EXISTS ciudades_id_ciudad_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS depositos_id_deposito_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS estado_solicitud_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS estados_contenedor_id_estado_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tipo_tramo_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS estado_tramo_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS estado_camion_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS clientes_id_cliente_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS contenedores_id_contenedor_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tarifas_id_tarifa_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS camiones_id_camion_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS solicitudes_id_solicitud_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS rutas_id_ruta_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tramos_id_tramo_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tarifa_volumen_peso_id_seq RESTART WITH 1;

-- =====================================================
-- 1. CIUDADES
-- =====================================================
INSERT INTO ciudades (id_ciudad, nombre) VALUES
(1, 'Buenos Aires'),
(2, 'Rosario'),
(3, 'Córdoba'),
(4, 'Mendoza'),
(5, 'La Plata'),
(6, 'Mar del Plata'),
(7, 'Tucumán'),
(8, 'Salta'),
(9, 'Santa Fe');

-- =====================================================
-- 2. DEPÓSITOS (distribuidos por ciudades)
-- =====================================================
INSERT INTO depositos (nombre, direccion, latitud, longitud, id_ciudad, costo_estadia_diario) VALUES
-- Buenos Aires (2 depósitos)
('Depósito Central Buenos Aires', 'Av. Warnes 1500, CABA', -34.5942, -58.4501, 1, 5000.00),
('Depósito Logístico Retiro', 'Av. Antártida Argentina 1355, CABA', -34.5894, -58.3742, 1, 4500.00),

-- Rosario (2 depósitos)
('Depósito Puerto Rosario', 'Av. Carballo 300, Rosario', -32.9587, -60.6393, 2, 3500.00),
('Depósito Industrial Rosario', 'Av. Circunvalación 5500, Rosario', -32.9298, -60.6868, 2, 3200.00),

-- Córdoba (2 depósitos)
('Depósito Córdoba Centro', 'Av. Colón 5000, Córdoba', -31.4135, -64.1810, 3, 3800.00),
('Depósito Zona Franca Córdoba', 'Ruta 9 km 695, Córdoba', -31.3524, -64.2417, 3, 3600.00),

-- Mendoza (1 depósito)
('Depósito Mendoza Logística', 'Ruta 7 km 1057, Mendoza', -32.8908, -68.8272, 4, 4000.00),

-- La Plata (1 depósito)
('Depósito La Plata Puerto', 'Av. 122 y 32, La Plata', -34.9095, -57.9568, 5, 3000.00),

-- Mar del Plata (1 depósito)
('Depósito Mar del Plata', 'Ruta 2 km 398, Mar del Plata', -38.0161, -57.5598, 6, 3200.00),

-- Tucumán (1 depósito)
('Depósito Tucumán Norte', 'Av. Circunvalación 2500, Tucumán', -26.8241, -65.2226, 7, 3400.00),

-- Salta (1 depósito)
('Depósito Salta Logística', 'Ruta 9 km 1620, Salta', -24.7821, -65.4232, 8, 3600.00),

-- Santa Fe (1 depósito)
('Depósito Santa Fe Capital', 'Ruta 11 km 450, Santa Fe', -31.6107, -60.6973, 9, 3100.00);

-- =====================================================
-- 3. ESTADOS DE SOLICITUDES
-- =====================================================
INSERT INTO estado_solicitud (id, nombre) VALUES
(1, 'PENDIENTE'),
(2, 'EN_PROCESO'),
(3, 'EN_TRANSITO'),
(4, 'COMPLETADA'),
(5, 'CANCELADA');

-- =====================================================
-- 4. ESTADOS DE CONTENEDORES
-- =====================================================
INSERT INTO estados_contenedor (id_estado, nombre) VALUES
(1, 'DISPONIBLE'),
(2, 'EN_TRANSITO'),
(3, 'EN_DEPOSITO'),
(4, 'ENTREGADO');

-- =====================================================
-- 5. TIPOS DE TRAMO
-- =====================================================
INSERT INTO tipo_tramo (id, nombre) VALUES
(1, 'ORIGEN_A_DEPOSITO'),
(2, 'ENTRE_DEPOSITOS'),
(3, 'DEPOSITO_A_DESTINO');

-- =====================================================
-- 6. ESTADOS DE TRAMO
-- =====================================================
INSERT INTO estado_tramo (id, nombre) VALUES
(1, 'PENDIENTE'),
(2, 'EN_CURSO'),
(3, 'COMPLETADO'),
(4, 'CANCELADO');

-- =====================================================
-- 7. ESTADOS DE CAMIÓN
-- =====================================================
INSERT INTO estado_camion (id, nombre) VALUES
(1, 'DISPONIBLE'),
(2, 'EN_RUTA'),
(3, 'MANTENIMIENTO'),
(4, 'FUERA_DE_SERVICIO');

-- =====================================================
-- 8. CLIENTES
-- =====================================================
INSERT INTO clientes (nombre, email, telefono) VALUES
('Mercado Libre Argentina', 'logistica@mercadolibre.com.ar', '0800-222-6353'),
('Walmart Argentina', 'transporte@walmart.com.ar', '0810-999-9278'),
('Carrefour Argentina', 'logistica@carrefour.com.ar', '0800-444-8500'),
('Coto CICSA', 'envios@coto.com.ar', '0800-444-2686'),
('DIA Argentina', 'distribucion@dia.com.ar', '0800-666-0342'),
('Farmacity', 'logistica@farmacity.com.ar', '0810-999-3276'),
('La Anónima', 'transporte@laanonima.com.ar', '0800-222-2665'),
('Sodimac Argentina', 'envios@sodimac.com.ar', '0810-362-4663');

-- =====================================================
-- 9. CONTENEDORES
-- =====================================================
INSERT INTO contenedores (peso, volumen, estado_id, cliente_id) VALUES
-- Mercado Libre (3 contenedores)
(1200.50, 15.5, 1, 1),
(850.00, 12.0, 1, 1),
(2100.75, 25.0, 1, 1),

-- Walmart (2 contenedores)
(3500.00, 40.0, 1, 2),
(2800.00, 35.0, 1, 2),

-- Carrefour (2 contenedores)
(1900.00, 22.0, 1, 3),
(2200.50, 28.0, 1, 3),

-- Coto (1 contenedor)
(1500.00, 18.0, 1, 4),

-- DIA (2 contenedores)
(1100.00, 14.0, 1, 5),
(1300.00, 16.0, 1, 5),

-- Farmacity (1 contenedor)
(600.00, 8.5, 1, 6),

-- La Anónima (1 contenedor)
(2500.00, 30.0, 1, 7),

-- Sodimac (2 contenedores)
(3200.00, 38.0, 1, 8),
(2900.00, 33.0, 1, 8);

-- =====================================================
-- 10. TARIFAS
-- =====================================================
INSERT INTO tarifas (id_tarifa, costo_base_gestion_fijo, valor_litro_combustible) VALUES
(1, 15000.00, 950.50);

-- Rangos de tarifas por volumen y peso
INSERT INTO tarifa_volumen_peso (id_tarifa, volumen_min, volumen_max, peso_min, peso_max, costo_por_km_base) VALUES
(1, 0, 10, 0, 1000, 45.00),
(1, 10, 20, 1000, 2000, 65.00),
(1, 20, 30, 2000, 3000, 85.00),
(1, 30, 50, 3000, 5000, 110.00);

-- =====================================================
-- 11. CAMIONES
-- =====================================================
INSERT INTO camiones (dominio, marca, modelo, capacidad_peso_max, capacidad_volumen_max, 
                      nombre_transportista, costo_base, costo_por_km, numero_transportistas, 
                      disponible, activo) VALUES
-- Flota de camiones grandes
('AB123CD', 'Scania', 'R450', 5000.0, 50.0, 'Juan Pérez', 25000.0, 85.0, 1, true, true),
('EF456GH', 'Mercedes-Benz', 'Actros 2651', 4800.0, 48.0, 'Carlos Gómez', 24000.0, 80.0, 1, true, true),
('IJ789KL', 'Volvo', 'FH16', 5200.0, 52.0, 'Roberto Silva', 26000.0, 90.0, 1, true, true),

-- Flota de camiones medianos
('MN012OP', 'Ford', 'Cargo 1722', 3500.0, 35.0, 'Miguel Rodríguez', 18000.0, 65.0, 1, true, true),
('QR345ST', 'Volkswagen', 'Constellation', 3200.0, 32.0, 'Lucas Martínez', 17000.0, 60.0, 1, true, true),
('UV678WX', 'Iveco', 'Tector', 3000.0, 30.0, 'Diego López', 16000.0, 55.0, 1, true, true),

-- Flota de camiones pequeños
('YZ901AB', 'Renault', 'Master', 1500.0, 18.0, 'Fernando García', 12000.0, 45.0, 1, true, true),
('CD234EF', 'Peugeot', 'Boxer', 1200.0, 15.0, 'Javier Fernández', 11000.0, 40.0, 1, true, true),
('GH567IJ', 'Fiat', 'Ducato', 1300.0, 16.0, 'Andrés Torres', 11500.0, 42.0, 1, true, true),
('KL890MN', 'Citroën', 'Jumper', 1400.0, 17.0, 'Pablo Morales', 12000.0, 43.0, 1, true, true);

-- =====================================================
-- 12. SOLICITUDES REALISTAS
-- =====================================================

-- Solicitud 1: Mercado Libre - Buenos Aires a Rosario
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado, 
                        tiempo_estimado, tarifa_id) VALUES
(1, 1, -34.5942, -58.4501, -32.9587, -60.6393,
 'Depósito Central Buenos Aires', 'Depósito Puerto Rosario', 2, 
 45000.00, 4.5, 1);

-- Solicitud 2: Walmart - Buenos Aires a Córdoba (pasando por Rosario)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(4, 2, -34.5942, -58.4501, -31.4135, -64.1810,
 'Depósito Central Buenos Aires', 'Depósito Córdoba Centro', 2,
 85000.00, 9.0, 1);

-- Solicitud 3: Carrefour - Córdoba a Mendoza
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(6, 3, -31.4135, -64.1810, -32.8908, -68.8272,
 'Depósito Córdoba Centro', 'Depósito Mendoza Logística', 2,
 95000.00, 8.5, 1);

-- Solicitud 4: DIA - Buenos Aires a Mar del Plata (pasando por La Plata)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(9, 5, -34.5942, -58.4501, -38.0161, -57.5598,
 'Depósito Central Buenos Aires', 'Depósito Mar del Plata', 2,
 55000.00, 5.5, 1);

-- Solicitud 5: Sodimac - Tucumán a Salta
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(13, 8, -26.8241, -65.2226, -24.7821, -65.4232,
 'Depósito Tucumán Norte', 'Depósito Salta Logística', 2,
 75000.00, 6.0, 1);

-- =====================================================
-- 13. RUTAS CON MÚLTIPLES TRAMOS CONECTADOS
-- =====================================================

-- Ruta 1: Buenos Aires -> Rosario (1 tramo directo)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (1, 1, NOW());

-- Ruta 2: Buenos Aires -> Rosario -> Córdoba (2 tramos conectados)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (2, 2, NOW());

-- Ruta 3: Córdoba -> Mendoza (1 tramo directo)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (3, 3, NOW());

-- Ruta 4: Buenos Aires -> La Plata -> Mar del Plata (2 tramos conectados)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (4, 4, NOW());

-- Ruta 5: Tucumán -> Salta (1 tramo directo)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (5, 5, NOW());

-- =====================================================
-- 14. TRAMOS (conectados lógicamente)
-- =====================================================

-- RUTA 1: Buenos Aires -> Rosario (tramo directo)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long, 
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(1, 3, -34.5942, -58.4501, -32.9587, -60.6393, 
 298.5, 1, 2, 2, 'AB123CD', 25000.00, 
 NOW() - INTERVAL '2 hours', NOW() + INTERVAL '2 hours');

-- RUTA 2: Buenos Aires -> Rosario -> Córdoba (2 tramos)
-- Tramo 1: Buenos Aires -> Rosario
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real) VALUES
(1, 3, -34.5942, -58.4501, -32.9587, -60.6393,
 298.5, 2, 1, 3, 'EF456GH', 35000.00,
 NOW() - INTERVAL '1 day', NOW() - INTERVAL '20 hours',
 NOW() - INTERVAL '1 day', NOW() - INTERVAL '20 hours');

-- Tramo 2: Rosario -> Córdoba
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(3, 5, -32.9587, -60.6393, -31.4135, -64.1810,
 401.2, 2, 2, 2, 'EF456GH', 50000.00,
 NOW() - INTERVAL '18 hours', NOW() + INTERVAL '2 hours');

-- RUTA 3: Córdoba -> Mendoza (tramo directo de larga distancia)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(5, 7, -31.4135, -64.1810, -32.8908, -68.8272,
 515.0, 3, 2, 2, 'IJ789KL', 95000.00,
 NOW() + INTERVAL '1 hour', NOW() + INTERVAL '10 hours');

-- RUTA 4: Buenos Aires -> La Plata -> Mar del Plata (2 tramos)
-- Tramo 1: Buenos Aires -> La Plata
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real) VALUES
(1, 8, -34.5942, -58.4501, -34.9095, -57.9568,
 58.0, 4, 1, 3, 'MN012OP', 8000.00,
 NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours',
 NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours');

-- Tramo 2: La Plata -> Mar del Plata
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(8, 9, -34.9095, -57.9568, -38.0161, -57.5598,
 392.0, 4, 3, 2, 'MN012OP', 47000.00,
 NOW() - INTERVAL '4 hours', NOW() + INTERVAL '1 hour');

-- RUTA 5: Tucumán -> Salta (tramo directo)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(10, 11, -26.8241, -65.2226, -24.7821, -65.4232,
 311.0, 5, 2, 1, 'QR345ST', 75000.00,
 NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 6 hours');

-- Actualizar solicitudes con ruta_id
UPDATE solicitudes SET ruta_id = 1 WHERE id_solicitud = 1;
UPDATE solicitudes SET ruta_id = 2 WHERE id_solicitud = 2;
UPDATE solicitudes SET ruta_id = 3 WHERE id_solicitud = 3;
UPDATE solicitudes SET ruta_id = 4 WHERE id_solicitud = 4;
UPDATE solicitudes SET ruta_id = 5 WHERE id_solicitud = 5;

-- =====================================================
-- 15. MÁS SOLICITUDES Y RUTAS COMPLEJAS
-- =====================================================

-- Solicitud 6: Farmacity - Rosario a Santa Fe
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(11, 6, -32.9587, -60.6393, -31.6107, -60.6973,
 'Depósito Puerto Rosario', 'Depósito Santa Fe Capital', 2,
 35000.00, 3.0, 1);

-- Solicitud 7: La Anónima - Buenos Aires a Córdoba a Tucumán (ruta larga con 3 tramos)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(12, 7, -34.5942, -58.4501, -26.8241, -65.2226,
 'Depósito Central Buenos Aires', 'Depósito Tucumán Norte', 2,
 165000.00, 18.0, 1);

-- Solicitud 8: Coto - La Plata a Mar del Plata
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(8, 4, -34.9095, -57.9568, -38.0161, -57.5598,
 'Depósito La Plata Puerto', 'Depósito Mar del Plata', 2,
 48000.00, 5.0, 1);

-- Solicitud 9: Mercado Libre - Mendoza a Córdoba
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(2, 1, -32.8908, -68.8272, -31.4135, -64.1810,
 'Depósito Mendoza Logística', 'Depósito Córdoba Centro', 2,
 88000.00, 8.0, 1);

-- Solicitud 10: Sodimac - Salta a Tucumán a Córdoba (ruta de retorno con 2 tramos)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(14, 8, -24.7821, -65.4232, -31.4135, -64.1810,
 'Depósito Salta Logística', 'Depósito Córdoba Centro', 2,
 125000.00, 14.0, 1);

-- Solicitud 11: DIA - Córdoba a Rosario (completada)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        costo_final, tiempo_estimado, tiempo_real, tarifa_id) VALUES
(10, 5, -31.4135, -64.1810, -32.9587, -60.6393,
 'Depósito Córdoba Centro', 'Depósito Puerto Rosario', 4,
 52000.00, 51500.00, 5.5, 5.2, 1);

-- Solicitud 12: Walmart - Rosario a Buenos Aires (completada)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        costo_final, tiempo_estimado, tiempo_real, tarifa_id) VALUES
(5, 2, -32.9587, -60.6393, -34.5942, -58.4501,
 'Depósito Puerto Rosario', 'Depósito Central Buenos Aires', 4,
 45000.00, 44200.00, 4.5, 4.3, 1);

-- Solicitud 13: Carrefour - Buenos Aires a Mendoza vía Rosario y Córdoba (ruta compleja 3 tramos)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(7, 3, -34.5942, -58.4501, -32.8908, -68.8272,
 'Depósito Central Buenos Aires', 'Depósito Mendoza Logística', 1,
 145000.00, 16.0, 1);

-- Solicitud 14: Mercado Libre - Mar del Plata a La Plata (pendiente)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(3, 1, -38.0161, -57.5598, -34.9095, -57.9568,
 'Depósito Mar del Plata', 'Depósito La Plata Puerto', 1,
 52000.00, 5.0, 1);

-- Solicitud 15: Farmacity - Santa Fe a Rosario a Buenos Aires (pendiente, ruta de 2 tramos)
INSERT INTO solicitudes (contenedor_id, cliente_id, origen_lat, origen_long, destino_lat, destino_long,
                        direccion_origen, direccion_destino, estado_solicitud_id, costo_estimado,
                        tiempo_estimado, tarifa_id) VALUES
(11, 6, -31.6107, -60.6973, -34.5942, -58.4501,
 'Depósito Santa Fe Capital', 'Depósito Central Buenos Aires', 1,
 72000.00, 7.5, 1);

-- =====================================================
-- 16. RUTAS ADICIONALES
-- =====================================================

-- Ruta 6: Rosario -> Santa Fe
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (6, 6, NOW());

-- Ruta 7: Buenos Aires -> Rosario -> Córdoba -> Tucumán (3 tramos)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (7, 7, NOW());

-- Ruta 8: La Plata -> Mar del Plata
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (8, 8, NOW());

-- Ruta 9: Mendoza -> Córdoba
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (9, 9, NOW());

-- Ruta 10: Salta -> Tucumán -> Córdoba (2 tramos)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (10, 10, NOW());

-- Ruta 11: Córdoba -> Rosario (completada)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (11, 11, NOW() - INTERVAL '3 days');

-- Ruta 12: Rosario -> Buenos Aires (completada)
INSERT INTO rutas (id_ruta, id_solicitud, fecha_creacion) VALUES (12, 12, NOW() - INTERVAL '5 days');

-- =====================================================
-- 17. TRAMOS ADICIONALES
-- =====================================================

-- RUTA 6: Rosario -> Santa Fe (tramo corto)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(3, 12, -32.9587, -60.6393, -31.6107, -60.6973,
 168.0, 6, 2, 2, 'YZ901AB', 35000.00,
 NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 hours');

-- RUTA 7: Buenos Aires -> Rosario -> Córdoba -> Tucumán (3 tramos largos)
-- Tramo 1: Buenos Aires -> Rosario
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real, costo_real) VALUES
(1, 3, -34.5942, -58.4501, -32.9587, -60.6393,
 298.5, 7, 1, 3, 'AB123CD', 45000.00,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '4 hours',
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '4 hours', 44800.00);

-- Tramo 2: Rosario -> Córdoba
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real, costo_real) VALUES
(3, 5, -32.9587, -60.6393, -31.4135, -64.1810,
 401.2, 7, 2, 3, 'AB123CD', 60000.00,
 NOW() - INTERVAL '2 days' + INTERVAL '5 hours', NOW() - INTERVAL '1 day' + INTERVAL '14 hours',
 NOW() - INTERVAL '2 days' + INTERVAL '5 hours', NOW() - INTERVAL '1 day' + INTERVAL '14 hours', 59500.00);

-- Tramo 3: Córdoba -> Tucumán
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(5, 10, -31.4135, -64.1810, -26.8241, -65.2226,
 574.0, 7, 3, 2, 'AB123CD', 60000.00,
 NOW() - INTERVAL '1 day' + INTERVAL '15 hours', NOW() + INTERVAL '4 hours');

-- RUTA 8: La Plata -> Mar del Plata (tramo directo)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(8, 9, -34.9095, -57.9568, -38.0161, -57.5598,
 392.0, 8, 2, 2, 'CD234EF', 48000.00,
 NOW() - INTERVAL '3 hours', NOW() + INTERVAL '2 hours');

-- RUTA 9: Mendoza -> Córdoba (tramo directo)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(7, 5, -32.8908, -68.8272, -31.4135, -64.1810,
 515.0, 9, 2, 2, 'IJ789KL', 88000.00,
 NOW() + INTERVAL '12 hours', NOW() + INTERVAL '21 hours');

-- RUTA 10: Salta -> Tucumán -> Córdoba (2 tramos)
-- Tramo 1: Salta -> Tucumán
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(11, 10, -24.7821, -65.4232, -26.8241, -65.2226,
 311.0, 10, 1, 1, 'UV678WX', 55000.00,
 NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days' + INTERVAL '6 hours');

-- Tramo 2: Tucumán -> Córdoba
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada) VALUES
(10, 5, -26.8241, -65.2226, -31.4135, -64.1810,
 574.0, 10, 2, 1, 'UV678WX', 70000.00,
 NOW() + INTERVAL '3 days' + INTERVAL '7 hours', NOW() + INTERVAL '3 days' + INTERVAL '16 hours');

-- RUTA 11: Córdoba -> Rosario (completada)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real, costo_real) VALUES
(5, 3, -31.4135, -64.1810, -32.9587, -60.6393,
 401.2, 11, 2, 3, 'QR345ST', 52000.00,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '5 hours',
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '5 hours', 51500.00);

-- RUTA 12: Rosario -> Buenos Aires (completada)
INSERT INTO tramos (origen_deposito_id, destino_deposito_id, origen_lat, origen_long,
                   destino_lat, destino_long, distancia, ruta_id, tipo_tramo_id, estado_tramo_id,
                   camion_dominio, costo_aproximado, fecha_hora_inicio_estimada, fecha_hora_fin_estimada,
                   fecha_hora_inicio_real, fecha_hora_fin_real, costo_real) VALUES
(3, 1, -32.9587, -60.6393, -34.5942, -58.4501,
 298.5, 12, 2, 3, 'MN012OP', 45000.00,
 NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '4 hours',
 NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '4 hours', 44200.00);

-- Actualizar solicitudes con nuevas ruta_id
UPDATE solicitudes SET ruta_id = 6 WHERE id_solicitud = 6;
UPDATE solicitudes SET ruta_id = 7 WHERE id_solicitud = 7;
UPDATE solicitudes SET ruta_id = 8 WHERE id_solicitud = 8;
UPDATE solicitudes SET ruta_id = 9 WHERE id_solicitud = 9;
UPDATE solicitudes SET ruta_id = 10 WHERE id_solicitud = 10;
UPDATE solicitudes SET ruta_id = 11 WHERE id_solicitud = 11;
UPDATE solicitudes SET ruta_id = 12 WHERE id_solicitud = 12;

-- =====================================================
-- 18. MÁS CLIENTES
-- =====================================================
INSERT INTO clientes (nombre, email, telefono) VALUES
('Falabella Argentina', 'logistica@falabella.com.ar', '0810-222-3252'),
('Easy Argentina', 'transporte@easy.com.ar', '0810-444-3279'),
('Jumbo Retail', 'distribucion@jumbo.com.ar', '0800-122-5862'),
('Disco Supermercados', 'envios@disco.com.ar', '0800-999-3472'),
('Vea Supermercados', 'logistica@vea.com.ar', '0810-777-8832'),
('Andreani Logística', 'operaciones@andreani.com', '0810-122-2637'),
('OCA Correo', 'envios@oca.com.ar', '0810-999-6222'),
('Rappi Argentina', 'logistica@rappi.com.ar', '0800-777-2774');

-- =====================================================
-- 19. MÁS CONTENEDORES
-- =====================================================
INSERT INTO contenedores (peso, volumen, estado_id, cliente_id) VALUES
-- Falabella (2 contenedores)
(2600.00, 31.0, 1, 9),
(1800.00, 20.0, 2, 9),

-- Easy (2 contenedores)
(3100.00, 37.0, 1, 10),
(2700.00, 32.0, 1, 10),

-- Jumbo (3 contenedores)
(2200.00, 26.0, 1, 11),
(1950.00, 23.0, 2, 11),
(2400.00, 29.0, 1, 11),

-- Disco (2 contenedores)
(1650.00, 19.0, 1, 12),
(1850.00, 21.0, 3, 12),

-- Vea (1 contenedor)
(2050.00, 24.0, 1, 13),

-- Andreani (3 contenedores)
(980.00, 11.5, 2, 14),
(1120.00, 13.0, 2, 14),
(1450.00, 17.0, 1, 14),

-- OCA (2 contenedores)
(750.00, 9.0, 1, 15),
(890.00, 10.5, 2, 15),

-- Rappi (2 contenedores)
(520.00, 6.5, 2, 16),
(680.00, 8.0, 1, 16);

-- =====================================================
-- 20. MÁS CAMIONES
-- =====================================================
INSERT INTO camiones (dominio, marca, modelo, capacidad_peso_max, capacidad_volumen_max,
                      nombre_transportista, costo_base, costo_por_km, numero_transportistas,
                      disponible, activo) VALUES
-- Más camiones grandes
('OP123QR', 'DAF', 'XF105', 5100.0, 51.0, 'Martín Sánchez', 25500.0, 87.0, 1, true, true),
('ST456UV', 'MAN', 'TGX', 4900.0, 49.0, 'Sebastián Ruiz', 24500.0, 83.0, 1, true, true),

-- Más camiones medianos
('WX789YZ', 'International', '4300', 3300.0, 33.0, 'Gustavo Ramírez', 17500.0, 62.0, 1, true, true),
('AB012CD', 'Freightliner', 'M2-106', 3400.0, 34.0, 'Ricardo Vargas', 18000.0, 64.0, 1, true, true),
('EF345GH', 'Kenworth', 'T370', 3600.0, 36.0, 'Daniel Castro', 19000.0, 67.0, 1, true, true),

-- Más camiones pequeños
('IJ678KL', 'Mercedes-Benz', 'Sprinter', 1250.0, 15.5, 'Claudio Méndez', 11200.0, 41.0, 1, true, true),
('MN901OP', 'Volkswagen', 'Crafter', 1350.0, 16.5, 'Adrián Flores', 11800.0, 43.0, 1, true, true),
('QR234ST', 'Ram', 'ProMaster', 1450.0, 17.5, 'Facundo Ortiz', 12200.0, 44.0, 1, true, true);

-- =====================================================
-- RESUMEN DE DATOS INSERTADOS (ACTUALIZADO):
-- =====================================================
-- 9 Ciudades
-- 12 Depósitos estratégicamente distribuidos
-- 16 Clientes (empresas reales argentinas + logística)
-- 32 Contenedores con diferentes pesos/volúmenes
-- 18 Camiones (5 grandes, 5 medianos, 8 pequeños)
-- 15 Solicitudes (12 activas, 2 completadas, 1 pendiente)
-- 12 Rutas (10 activas, 2 completadas)
-- 24 Tramos total (conectados lógicamente)
--
-- RUTAS DETALLADAS:
-- Ruta 1: BA -> Rosario (1 tramo, ~300km) EN CURSO
-- Ruta 2: BA -> Rosario -> Córdoba (2 tramos, ~700km) EN CURSO
-- Ruta 3: Córdoba -> Mendoza (1 tramo, ~515km) EN CURSO
-- Ruta 4: BA -> La Plata -> Mar del Plata (2 tramos, ~450km) EN CURSO
-- Ruta 5: Tucumán -> Salta (1 tramo, ~311km) PROGRAMADA
-- Ruta 6: Rosario -> Santa Fe (1 tramo, ~168km) EN CURSO
-- Ruta 7: BA -> Rosario -> Córdoba -> Tucumán (3 tramos, ~1274km) EN CURSO
-- Ruta 8: La Plata -> Mar del Plata (1 tramo, ~392km) EN CURSO
-- Ruta 9: Mendoza -> Córdoba (1 tramo, ~515km) PROGRAMADA
-- Ruta 10: Salta -> Tucumán -> Córdoba (2 tramos, ~885km) PROGRAMADA
-- Ruta 11: Córdoba -> Rosario (1 tramo, ~401km) COMPLETADA
-- Ruta 12: Rosario -> BA (1 tramo, ~298km) COMPLETADA
-- =====================================================
