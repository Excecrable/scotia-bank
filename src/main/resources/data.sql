-- CLIENTES
INSERT INTO client(id, name, address, phone, status)
VALUES (1, 'RONEL', 'Bogotá', '312-4398183', 'ENABLED');
INSERT INTO client(id, name, address, phone, status)
VALUES (2, 'MARÍA', 'Medellin', '310-5556677', 'ENABLED');
INSERT INTO client(id, name, address, phone, status)
VALUES (3, 'JUAN', 'Cali', '312-8889900', 'DISABLED');
INSERT INTO client(id, name, address, phone, status)
VALUES (4, 'ANA', 'Cartagena', '311-3334422', 'ENABLED');

-- CUENTAS
INSERT INTO account(id, client_id, number, balance, status)
VALUES (1, 1, '1111-2222-3333-4444', 2500000.00, 'ENABLED');
INSERT INTO account(id, client_id, number, balance, status)
VALUES (2, 1, '5555-6666-7777-8888', 725000.75, 'ENABLED');
INSERT INTO account(id, client_id, number, balance, status)
VALUES (3, 1, '6666-7777-8888-9999', 0.00, 'DISABLED');
INSERT INTO account(id, client_id, number, balance, status)
VALUES (4, 2, '2222-3333-4444-5555', 1369000.00, 'ENABLED');
INSERT INTO account(id, client_id, number, balance, status)
VALUES (5, 2, '1234-9876-5698-3274', 476987.50, 'ENABLED');