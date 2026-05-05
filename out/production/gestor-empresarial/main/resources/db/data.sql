-- Dataset estratégico de inicialización - Entorno Corporativo 2026
-- Consolidado de capital humano, cartera de proyectos y matriz de asignación.

-- Limpieza controlada de tablas (Desactivación temporal de integridad)
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM PR_EMPLEADOS_PROYECTO;
DELETE FROM PR_PROYECTOS;
DELETE FROM EM_EMPLEADOS;
DELETE FROM US_USUARIOS;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Acceso Maestro (Usuario Administrador)
-- Password: 12345678S* (Cifrada con BCrypt)
INSERT INTO US_USUARIOS (TX_EMAIL, TX_PASSWORD, TX_ROLE, F_ALTA) VALUES 
('admin@futurespace.com', '$2a$10$6l8lrZLFcDdU7Kxo4y9Zb.bCrKm9XPN9i5hVpDmdYCYLlIRrmT6LK', 'ADMIN', CURDATE()),
('susana@futurespace.com', '$2a$10$6l8lrZLFcDdU7Kxo4y9Zb.bCrKm9XPN9i5hVpDmdYCYLlIRrmT6LK', 'ADMIN', CURDATE());

-- 2. Entidades de capital humano (20 Empleados)
INSERT INTO EM_EMPLEADOS (TX_NIF, TX_NOMBRE, TX_APELLIDO1, TX_APELLIDO2, F_NACIMIENTO, N_TELEFONO1, N_TELEFONO2, TX_EMAIL, F_ALTA, F_BAJA, CX_EDOCIVIL, B_FORMACIONU) VALUES
('12345678A', 'Susana', 'Bitar', 'Azevedo', '1995-05-20', '600111222', '911222333', 'susana.bitar@futurespace.com', '2023-01-10', NULL, 'S', 'S'),
('23456789B', 'Carlos', 'García', 'Ruiz', '1988-03-15', '600222333', '911333444', 'carlos.garcia@futurespace.com', '2022-05-20', NULL, 'C', 'S'),
('34567890C', 'Elena', 'Martín', 'Sanz', '1992-11-02', '600333444', '911444555', 'elena.martin@futurespace.com', '2023-03-15', NULL, 'S', 'S'),
('45678901D', 'Javier', 'López', 'Mora', '1990-07-22', '600444555', '911555666', 'javier.lopez@futurespace.com', '2021-09-01', NULL, 'C', 'N'),
('56789012E', 'Beatriz', 'Núñez', 'Vera', '1997-12-12', '600555666', '911666777', 'beatriz.nunez@futurespace.com', '2024-01-15', NULL, 'S', 'S'),
('67890123F', 'Ricardo', 'Soto', 'Blanco', '1985-06-30', '600666777', '911777888', 'ricardo.soto@futurespace.com', '2020-02-10', NULL, 'C', 'S'),
('78901234G', 'Sofía', 'Vega', 'Luna', '1993-09-18', '600777888', '911888999', 'sofia.vega@futurespace.com', '2023-05-20', NULL, 'S', 'S'),
('89012345H', 'Marcos', 'Pérez', 'Gómez', '1991-04-05', '600888999', '911999000', 'marcos.perez@futurespace.com', '2022-11-01', NULL, 'C', 'N'),
('90123456I', 'Laura', 'Díaz', 'Ríos', '1996-02-28', '600999000', '911000111', 'laura.diaz@futurespace.com', '2024-02-01', NULL, 'S', 'S'),
('01234567J', 'Alberto', 'Cano', 'Ruiz', '1987-08-14', '600000111', '911111222', 'alberto.cano@futurespace.com', '2021-01-15', NULL, 'C', 'S'),
('11223344K', 'Marta', 'Gil', 'Lara', '1994-10-10', '600112233', '911223344', 'marta.gil@futurespace.com', '2023-07-01', NULL, 'S', 'S'),
('22334455L', 'Víctor', 'Ramos', 'Ponce', '1989-12-05', '600223344', '911334455', 'victor.ramos@futurespace.com', '2022-03-20', NULL, 'C', 'S'),
('33445566M', 'Paula', 'Sánchez', 'Torres', '1992-05-15', '600334455', '911445566', 'paula.sanchez@futurespace.com', '2023-10-10', NULL, 'S', 'N'),
('44556677N', 'Raúl', 'Castro', 'Vidal', '1990-01-20', '600445566', '911556677', 'raul.castro@futurespace.com', '2021-06-15', NULL, 'C', 'S'),
('55667788O', 'Isabel', 'Luna', 'Ferrer', '1998-03-03', '600556677', '911667788', 'isabel.luna@futurespace.com', '2024-03-01', NULL, 'S', 'S'),
('66778899P', 'Hugo', 'Mora', 'Reyes', '1986-11-11', '600667788', '911778899', 'hugo.mora@futurespace.com', '2020-09-01', NULL, 'C', 'S'),
('77889900Q', 'Sara', 'Vila', 'Ortega', '1993-07-07', '600778899', '911889900', 'sara.vila@futurespace.com', '2023-12-01', NULL, 'S', 'S'),
('88990011R', 'Daniel', 'Pascual', 'Cid', '1989-05-25', '600889900', '911990011', 'daniel.pascual@futurespace.com', '2022-04-01', NULL, 'C', 'S'),
('99001122S', 'Carmen', 'Marín', 'Ramos', '1995-07-14', '600990011', '911001122', 'carmen.marin@futurespace.com', '2023-09-01', NULL, 'S', 'N'),
('10112233T', 'Jorge', 'Lorenzo', 'Soto', '1992-03-18', '600001122', '911112233', 'jorge.lorenzo@futurespace.com', '2024-01-10', NULL, 'C', 'S');

-- 3. Cartera de proyectos (10 Proyectos)
INSERT INTO PR_PROYECTOS (TX_DESCRIPCION, F_INICIO, F_FIN, F_BAJA, TX_LUGAR, TX_OBSERVACIONES) VALUES
('Migración Cloud Azure 2026', '2024-01-01', '2024-12-31', NULL, 'Madrid - Central', 'Iniciativa de modernización de infraestructura crítica.'),
('Portal de Inteligencia Prescriptiva', '2024-02-15', '2025-06-30', NULL, 'Remoto', 'Motor de analítica avanzada basado en modelos predictivos.'),
('Sistema de Gestión Logística v2', '2023-11-01', '2024-08-30', NULL, 'Barcelona', 'Optimización de flujos de cadena de suministro global.'),
('Auditoría Ciberseguridad Global', '2024-03-01', '2024-05-30', NULL, 'Valencia', 'Evaluación integral de perímetros y protocolos.'),
('App Móvil Fidelización Plus', '2023-05-10', '2023-12-20', '2023-12-31', 'Madrid - Norte', 'Registro histórico: Proyecto concluido.'),
('Modernización Core Bancario', '2024-04-01', '2025-12-31', NULL, 'Sevilla', 'Arquitectura de microservicios sobre sistemas legados.'),
('Rediseño UX/UI Intranet', '2024-05-01', '2024-09-30', NULL, 'Málaga', 'Mejora de la experiencia de usuario corporativa.'),
('Integración de Pasarela Pagos', '2024-01-15', '2024-06-30', NULL, 'Bilbao', 'Implementación de nuevos métodos de pago seguros.'),
('Mantenimiento Preventivo Servidores', '2023-01-01', '2023-12-31', '2023-12-31', 'Madrid - DataCenter', 'Histórico de mantenimiento anual.'),
('Expansión Infraestructura Red', '2024-06-01', '2024-12-31', NULL, 'Zaragoza', 'Despliegue de fibra óptica y nuevos nodos.');

-- 4. Matriz de asignación
INSERT INTO PR_EMPLEADOS_PROYECTO (ID_PROYECTO, ID_EMPLEADO, F_ALTA) VALUES 
(1, 1, '2024-01-01'), (1, 2, '2024-01-01'), (1, 3, '2024-01-15'), (1, 14, '2024-01-20'),
(2, 1, '2024-02-15'), (2, 2, '2024-02-15'), (2, 4, '2024-03-01'), (2, 5, '2024-03-01'), (2, 13, '2024-03-05'),
(3, 3, '2023-11-01'), (3, 6, '2023-11-01'), (3, 7, '2023-12-01'), (3, 15, '2024-01-10'),
(4, 8, '2024-03-01'), (4, 9, '2024-03-15'), (4, 17, '2024-03-20'),
(6, 11, '2024-04-01'), (6, 12, '2024-04-01'), (6, 18, '2024-04-05'),
(7, 3, '2024-05-01'), (7, 5, '2024-05-01'), (7, 13, '2024-05-10'),
(8, 2, '2024-01-15'), (8, 9, '2024-01-15'), (8, 16, '2024-02-01'),
(10, 19, '2024-06-01'), (10, 20, '2024-06-01');
