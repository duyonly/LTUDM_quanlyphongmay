
-- DATABASE: quản lý phòng máy
--postgresql

-- 1. XOA BANG CU NEU DA TON TAI
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS commands CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS machine_specs CASCADE;
DROP TABLE IF EXISTS machines CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. BANG USERS
-- Quan ly tai khoan Admin / Staff

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check
        CHECK (role IN ('ADMIN', 'STAFF'))
);

-- 3. BANG MACHINES
-- Luu thong tin cac may trong phong may

CREATE TABLE machines (
    id BIGSERIAL PRIMARY KEY,
    machine_code VARCHAR(50) NOT NULL UNIQUE,
    hostname VARCHAR(100),
    ip_address VARCHAR(45),
    os VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_seen TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT machines_status_check
        CHECK (status IN ('ONLINE', 'OFFLINE', 'LOCKED'))
);

-- 4. BANG MACHINE_SPECS
-- Thong tin cau hinh phan cung cua may
-- Moi machine chi co 1 bo thong tin cau hinh

CREATE TABLE machine_specs (
    id BIGSERIAL PRIMARY KEY,
    machine_id BIGINT NOT NULL UNIQUE,
    cpu VARCHAR(255),
    ram VARCHAR(100),
    gpu VARCHAR(255),
    disk VARCHAR(255),
    CONSTRAINT fk_machine_specs_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE CASCADE
);

-- 5. BANG SESSIONS
-- Luu lich su phien lam viec / ket noi

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    machine_id BIGINT NOT NULL,
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP,
    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_sessions_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE CASCADE
);

-- 6. BANG COMMANDS
-- Luu cac lenh Admin gui den Machine
-- request_id ket noi voi Message.requestId trong Java

CREATE TABLE commands (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    machine_id BIGINT NOT NULL,
    admin_id BIGINT,
    command_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_commands_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_commands_admin
        FOREIGN KEY (admin_id)
        REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT commands_status_check
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

-- 7. BANG AUDIT_LOGS
-- Luu lich su thao tac cua Admin / Staff

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- 8. DU LIEU USERS

INSERT INTO users (
    username,
    password_hash,
    role
)
VALUES
(
    'admin',
    '123456',
    'ADMIN'
),
(
    'staff01',
    '123456',
    'STAFF'
);

-- 9. DU LIEU MACHINES

INSERT INTO machines (
    machine_code,
    hostname,
    ip_address,
    os,
    status
)
VALUES
(
    'PC01',
    'LAB-PC01',
    '192.168.1.101',
    'Windows 11',
    'OFFLINE'
),
(
    'PC02',
    'LAB-PC02',
    '192.168.1.102',
    'Windows 11',
    'OFFLINE'
),
(
    'PC03',
    'LAB-PC03',
    '192.168.1.103',
    'Windows 11',
    'OFFLINE'
),
(
    'PC04',
    'LAB-PC04',
    '192.168.1.104',
    'Windows 11',
    'OFFLINE'
);

-- 10. DU LIEU MACHINE SPECS

INSERT INTO machine_specs (
    machine_id,
    cpu,
    ram,
    gpu,
    disk
)
SELECT
    id,
    'Intel Core i5',
    '8GB',
    'Intel UHD Graphics',
    '256GB SSD'
FROM machines
WHERE machine_code = 'PC01';

INSERT INTO machine_specs (
    machine_id,
    cpu,
    ram,
    gpu,
    disk
)
SELECT
    id,
    'Intel Core i5',
    '8GB',
    'Intel UHD Graphics',
    '256GB SSD'
FROM machines
WHERE machine_code = 'PC02';

INSERT INTO machine_specs (
    machine_id,
    cpu,
    ram,
    gpu,
    disk
)
SELECT
    id,
    'Intel Core i5',
    '16GB',
    'Intel UHD Graphics',
    '512GB SSD'
FROM machines
WHERE machine_code = 'PC03';

INSERT INTO machine_specs (
    machine_id,
    cpu,
    ram,
    gpu,
    disk
)
SELECT
    id,
    'Intel Core i7',
    '16GB',
    'NVIDIA GTX',
    '512GB SSD'
FROM machines
WHERE machine_code = 'PC04';

-- 11. KIEM TRA DU LIEU

SELECT * FROM users;
SELECT * FROM machines;
SELECT * FROM machine_specs;
SELECT * FROM sessions;
SELECT * FROM commands;
SELECT * FROM audit_logs;

-- 12. KIEM TRA QUAN HE MACHINES + SPECS

SELECT
    m.machine_code,
    m.hostname,
    m.ip_address,
    m.os,
    m.status,
    ms.cpu,
    ms.ram,
    ms.gpu,
    ms.disk
FROM machines m
LEFT JOIN machine_specs ms
    ON m.id = ms.machine_id
ORDER BY m.machine_code;