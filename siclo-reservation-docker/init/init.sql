-- Crear esquema (opcional, puedes usar "public")
CREATE SCHEMA IF NOT EXISTS sport_center;
SET search_path TO sport_center;

-- ========================
-- Tabla de clientes
-- ========================
CREATE TABLE client (
    client_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    email          VARCHAR(150),
    phone          VARCHAR(50),
    document_id    VARCHAR(50),
    created_at     TIMESTAMP DEFAULT now()
);

-- ========================
-- Tabla de estudios/locales
-- ========================
CREATE TABLE studio (
    studio_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    country        VARCHAR(100),
    city           VARCHAR(100),
    room           VARCHAR(100),
    created_at     TIMESTAMP DEFAULT now()
);

-- ========================
-- Tabla de disciplinas
-- ========================
CREATE TABLE discipline (
    discipline_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(100) UNIQUE NOT NULL
);

-- ========================
-- Tabla de instructores
-- ========================
CREATE TABLE instructor (
    instructor_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) NOT NULL
);

-- ========================
-- Tabla de reservas
-- ========================
CREATE TABLE reservation (
    reservation_id   BIGINT PRIMARY KEY,
    class_id         BIGINT NOT NULL,
    studio_id        BIGINT NOT NULL,
    discipline_id    BIGINT NOT NULL,
    instructor_id    BIGINT NOT NULL,
    client_id        BIGINT NOT NULL,
    guide            VARCHAR(150),
    time             TIMESTAMP,
    order_creator    VARCHAR(150),
    payment_method   VARCHAR(100),
    status           VARCHAR(50),
    created_at       TIMESTAMP DEFAULT now()
);

-- ========================
-- Tabla de pagos
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
    created_at         TIMESTAMP DEFAULT now()
);

-- ========================
-- Llaves foráneas
-- ========================
ALTER TABLE reservation
  ADD CONSTRAINT fk_reservation_studio FOREIGN KEY (studio_id) REFERENCES studio(studio_id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_reservation_discipline FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_reservation_instructor FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_reservation_client FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE;

ALTER TABLE payment_transaction
  ADD CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE;

-- ========================
-- Índices útiles
-- ========================
CREATE INDEX idx_reservation_studio      ON reservation(studio_id);
CREATE INDEX idx_reservation_client      ON reservation(client_id);
CREATE INDEX idx_reservation_discipline  ON reservation(discipline_id);
CREATE INDEX idx_payment_client          ON payment_transaction(client_id);
CREATE INDEX idx_payment_method          ON payment_transaction(payment_method);
