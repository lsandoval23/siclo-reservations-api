-- ========================
-- Drops
-- ========================
DROP TABLE IF EXISTS payment_transaction CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS studio CASCADE;
DROP TABLE IF EXISTS discipline CASCADE;
DROP TABLE IF EXISTS instructor CASCADE;
DROP TABLE IF EXISTS client CASCADE;

-- ========================
-- Client Table
-- ========================
CREATE TABLE client (
    client_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) ,
    email          VARCHAR(150) UNIQUE NOT NULL,
    phone          VARCHAR(50),
    document_id    VARCHAR(50),
    created_at     TIMESTAMP DEFAULT now()
);

-- ========================
-- Studio Table
-- ========================
CREATE TABLE studio (
    studio_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) UNIQUE NOT NULL,
    country        VARCHAR(100),
    city           VARCHAR(100),
    created_at     TIMESTAMP DEFAULT now()
);

-- ========================
-- Room Table
-- ========================
CREATE TABLE room (
    room_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    studio_id      BIGINT NOT NULL,
    name           VARCHAR(100) UNIQUE NOT NULL,
    created_at     TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_room_studio FOREIGN KEY (studio_id) REFERENCES studio(studio_id) ON DELETE CASCADE
);

-- ========================
-- Discipline Table
-- ========================
CREATE TABLE discipline (
    discipline_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(100) UNIQUE NOT NULL
);

-- ========================
-- Instructor Table
-- ========================
CREATE TABLE instructor (
    instructor_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) UNIQUE NOT NULL
);

-- ========================
-- Reservation Table
-- ========================
CREATE TABLE reservation (
    reservation_id   BIGINT PRIMARY KEY,
    class_id         BIGINT NOT NULL,
    room_id          BIGINT NOT NULL,
    discipline_id    BIGINT NOT NULL,
    instructor_id    BIGINT NOT NULL,
    client_id        BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    order_creator    VARCHAR(150),
    payment_method   VARCHAR(100),
    status           VARCHAR(50),
    created_at       TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_reservation_room       FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_discipline FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_instructor FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_client     FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);

-- ========================
-- User Table
-- ========================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- Roles Table
-- ========================

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- Permissions Table
-- ========================

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(50),
    action VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================
-- User Roles Table
-- ========================

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ========================
-- Role permissions Table
-- ========================

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ========================
-- Payment Transaction Table
-- ========================
CREATE TABLE payment_transaction (
    transaction_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id          BIGINT NOT NULL,
    month              INT,
    day                INT,
    week               INT,
    purchase_date      DATE,
    accreditation_date DATE,
    release_date       DATE,
    operation_type     VARCHAR(100),
    product_value      NUMERIC(12,2),
    transaction_fee    NUMERIC(12,2),
    amount_received    NUMERIC(12,2),
    installments       INT,
    payment_method     VARCHAR(100),
    package            VARCHAR(100),
    class_count        INT,
    created_at         TIMESTAMP DEFAULT now(),
    CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);

-- ========================
-- Indexes
-- ========================
CREATE INDEX idx_room_studio             ON room(studio_id);
CREATE INDEX idx_reservation_room        ON reservation(room_id);
CREATE INDEX idx_reservation_client      ON reservation(client_id);
CREATE INDEX idx_reservation_discipline  ON reservation(discipline_id);
CREATE INDEX idx_payment_client          ON payment_transaction(client_id);
CREATE INDEX idx_payment_method          ON payment_transaction(payment_method);

-- ========================
-- Functions
-- ========================

CREATE OR REPLACE FUNCTION get_reservations_report(
    group_by TEXT,
    from_date DATE,
    to_date DATE
)
RETURNS TABLE(group_name TEXT, reservation_date DATE, total BIGINT)
LANGUAGE sql
AS $$
SELECT
    (CASE
        WHEN group_by = 'studio'    THEN s.name
        WHEN group_by = 'instructor' THEN i.name
        WHEN group_by = 'discipline' THEN d.name
    END)::text AS group_name,
    r.reservation_date,
    COUNT(r.reservation_id)::bigint AS total
FROM reservation r
LEFT JOIN room rm      ON r.room_id = rm.room_id
LEFT JOIN studio s     ON rm.studio_id = s.studio_id
LEFT JOIN instructor i ON r.instructor_id = i.instructor_id
LEFT JOIN discipline d ON r.discipline_id = d.discipline_id
WHERE r.reservation_date BETWEEN from_date AND to_date
GROUP BY group_name, r.reservation_date
ORDER BY group_name, r.reservation_date;
$$;

-- ========================
-- Add start items
-- ========================

-- Create basic permissions
INSERT INTO permissions (name, description, resource, action) VALUES
('USER_READ', 'Read user information', 'USER', 'READ'),
('USER_CREATE', 'Create new users', 'USER', 'CREATE'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
('ROLE_READ', 'Read role information', 'ROLE', 'READ'),
('ROLE_MANAGE', 'Manage roles and permissions', 'ROLE', 'MANAGE'),
('POST_READ', 'Read posts', 'POST', 'READ'),
('POST_CREATE', 'Create posts', 'POST', 'CREATE'),
('POST_UPDATE', 'Update posts', 'POST', 'UPDATE'),
('POST_DELETE', 'Delete posts', 'POST', 'DELETE'),
('POST_ADMIN', 'Full admin access to posts', 'POST', 'ADMIN');

-- Create basic roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('MODERATOR', 'Moderator with limited admin access'),
('USER', 'Regular user');

-- Assign permissions to role: ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- Assign permissions to role: MODERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MODERATOR'
AND p.name IN ('USER_READ', 'POST_READ', 'POST_CREATE', 'POST_UPDATE', 'POST_ADMIN');

-- Assign permissions to role: USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'USER'
AND p.name IN ('USER_READ', 'POST_READ');

-- Create default admin user (password: admin123)
-- Password hashed using bcrypt
INSERT INTO users (username, email, password, first_name, last_name) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFV5Z1.DfFSBnqh0xkOTLce', 'Admin', 'User');

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';