-- ==========================================================
-- RESETEO Y SEMILLA COMPLETA - UrbanActive (Final)
-- ==========================================================
USE urbanactive;

SET SQL_SAFE_UPDATES = 0;

-- Este archivo limpia la base de datos y añade actividades con los deportes
-- solicitados (Baloncesto, Running, Ciclismo, Futbol, Natacion, Yoga)
-- repartidos en diferentes ubicaciones para que se vean todos en el mapa.

-- ----------------------------------------------------------
-- 0. LIMPIEZA TOTAL
-- ----------------------------------------------------------
DELETE FROM reserva;
DELETE FROM informe_meteorologico;
DELETE FROM actividad;
DELETE FROM ubicacion;

-- ----------------------------------------------------------
-- 1. UBICACIONES (Diferentes puntos para distribuir los marcadores)
-- ----------------------------------------------------------
INSERT INTO ubicacion (Id, nombre, latitud, longitud, tipo_espacio) VALUES 
('UBI01', 'Retiro - Zona Norte', 40.4190, -3.6880, 'EXTERIOR'),
('UBI02', 'Retiro - Estanque', 40.4173, -3.6835, 'EXTERIOR'),
('UBI03', 'Retiro - Palacio Cristal', 40.4137, -3.6820, 'EXTERIOR'),
('UBI04', 'Retiro - Rosaleda', 40.4103, -3.6816, 'EXTERIOR'),
('UBI05', 'Retiro - Florida Park', 40.4205, -3.6810, 'EXTERIOR'),
('UBI06', 'Retiro - Paseo Coches', 40.4150, -3.6850, 'EXTERIOR');

-- ----------------------------------------------------------
-- 2. ACTIVIDADES (Deportes: Baloncesto, Running, Ciclismo, Futbol, Natacion, Yoga)
-- ----------------------------------------------------------
INSERT INTO actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id, fecha_cancelacion) VALUES 
('A01', 'Baloncesto', '2026-05-10 18:00:00', 12, 'ABIERTA', 'UBI01', 'ORG001', NULL),
('A02', 'Running',    '2026-05-12 17:30:00', 20, 'ABIERTA', 'UBI02', 'ORG001', NULL),
('A03', 'Ciclismo',   '2026-05-14 09:00:00', 10, 'ABIERTA', 'UBI03', 'ORG001', NULL),
('A04', 'Futbol',     '2026-05-15 11:00:00', 22, 'ABIERTA', 'UBI04', 'ORG001', NULL),
('A05', 'Natacion',   '2026-05-20 10:00:00', 8,  'ABIERTA', 'UBI05', 'ORG001', NULL),
('A06', 'Yoga',       '2026-05-22 20:00:00', 15, 'ABIERTA', 'UBI06', 'ORG001', NULL);

-- ----------------------------------------------------------
-- ESCENARIOS ADICIONALES (Notificaciones)
-- ----------------------------------------------------------
INSERT INTO actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id, fecha_cancelacion) VALUES 
('N_FULL', 'Baloncesto', '2026-06-01 10:00:00', 1,  'COMPLETA', 'UBI01', 'ORG001', NULL),
('N_CANC', 'Yoga',       '2026-06-05 18:00:00', 10, 'CANCELADA', 'UBI06', 'ORG001', '2026-04-20 12:00:00'),
('N_WEAT', 'Running',    '2026-04-21 20:00:00', 20, 'ABIERTA',   'UBI02', 'ORG001', NULL);

INSERT INTO reserva (id, fecha_reserva, estado, id_usuario, id_actividad) VALUES 
('R_F1', '2026-04-20 12:00:00', 'CONFIRMADA', 'US001', 'N_FULL'),
('R_C1', '2026-04-20 12:00:00', 'CONFIRMADA', 'US001', 'N_CANC'),
('R_W1', '2026-04-20 12:00:00', 'CONFIRMADA', 'US001', 'N_WEAT');

-- ----------------------------------------------------------
-- INFORMES METEOROLOGICOS FIJOS DE PRUEBA
-- ----------------------------------------------------------
INSERT INTO informe_meteorologico (Id, temperatura, calidad_aire, id_actividad, probabilidad_lluvia, ultima_actualizacion, fecha_datos, id_ubicacion) VALUES 
('W_A01', 20.0, 30, 'A01', 0,  '2026-04-20 17:00:00', '2026-05-10 18:00:00', 'UBI01'),
('W_A02', 18.5, 25, 'A02', 5,  '2026-04-20 17:00:00', '2026-05-12 17:30:00', 'UBI02'),
('W_A03', 22.1, 40, 'A03', 0,  '2026-04-20 17:00:00', '2026-05-14 09:00:00', 'UBI03'),
('W_A04', 19.8, 35, 'A04', 10, '2026-04-20 17:00:00', '2026-05-15 11:00:00', 'UBI04'),
('W_A05', 25.0, 20, 'A05', 0,  '2026-04-20 17:00:00', '2026-05-20 10:00:00', 'UBI05'),
('W_A06', 21.0, 30, 'A06', 0,  '2026-04-20 17:00:00', '2026-05-22 20:00:00', 'UBI06'),
('W_NF',  22.0, 30, 'N_FULL', 0, '2026-04-20 17:00:00', '2026-06-01 10:00:00', 'UBI01'),
('W_NC',  23.0, 30, 'N_CANC', 0, '2026-04-20 17:00:00', '2026-06-05 18:00:00', 'UBI06'),
('W_W01', 1.5, 20, 'N_WEAT', 0, '2026-04-20 17:00:00', '2026-04-21 20:00:00', 'UBI02');

SET SQL_SAFE_UPDATES = 1;
