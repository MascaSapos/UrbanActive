-- ==========================================================
-- LISTADO DE SEGUIMIENTO Y DATOS DE PRUEBA - UrbanActive
-- ==========================================================

-- 1. BASE REQUERIDA
-- ----------------------------------------------------------
INSERT INTO Ubicacion (Id, nombre, latitud, longitud, tipo_espacio) VALUES ('UBI01', 'Retiro Madrid', 40.41, -3.68, 'EXTERIOR');


-- 2. ACTIVIDADES VARIADAS (PARA RELLENAR EL MAPA)
-- ----------------------------------------------------------
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A01', 'Fútbol', '2026-05-10 18:00:00', 22, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A02', 'Baloncesto', '2026-05-12 17:30:00', 12, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A03', 'Pádel', '2026-05-14 09:00:00', 4, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A04', 'Tenis', '2026-05-15 11:00:00', 2, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A05', 'Yoga', '2026-05-20 10:00:00', 15, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('A06', 'Running', '2026-05-22 20:00:00', 30, 'ABIERTA', 'UBI01', 'ORG001');


-- (OPCIONAL). ESCENARIOS ESPECÍFICOS PARA TEST DE NOTIFICACIONES
-- ----------------------------------------------------------

-- ESCENARIO A: Actividad Completa (Para que el Organizador reciba aviso)
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('N_FULL', 'Tenis', '2026-06-01 10:00:00', 1, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Reserva (id, fecha_reserva, estado, id_usuario, id_actividad) VALUES ('R_F1', '2026-04-20 12:00:00', 'CONFIRMADA', 'USR001', 'N_FULL');

-- ESCENARIO B: Actividad Cancelada (Para que el Deportista reciba aviso)
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('N_CANC', 'Yoga', '2026-06-05 18:00:00', 10, 'CANCELADA', 'UBI01', 'ORG001');
INSERT INTO Reserva (id, fecha_reserva, estado, id_usuario, id_actividad) VALUES ('R_C1', '2026-04-20 12:00:00', 'CONFIRMADA', 'USR001', 'N_CANC');

-- ESCENARIO C: Alerta de Mal Clima (Para que ambos reciban aviso)
INSERT INTO Actividad (Id, tipo_deporte, fecha_hora, plazas_total, estado, id_ubicacion, organizador_id) VALUES ('N_WEAT', 'Running', '2026-04-21 20:00:00', 20, 'ABIERTA', 'UBI01', 'ORG001');
INSERT INTO Reserva (id, fecha_reserva, estado, id_usuario, id_actividad) VALUES ('R_W1', '2026-04-20 12:00:00', 'CONFIRMADA', 'USR001', 'N_WEAT');
-- Informe de clima frío (1.5 grados) para disparar la alerta
INSERT INTO InformeMeteorologico (Id, temperatura, calidad_aire, id_actividad, probabilidad_lluvia, ultima_actualizacion, fecha_datos, id_ubicacion) VALUES ('W_01', 1.5, 20, 'N_WEAT', 0, '2026-04-20 17:00:00', '2026-04-21 20:00:00', 'UBI01');
