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
    name           VARCHAR(150) UNIQUE NOT NULL,
    email          VARCHAR(150) NOT NULL,
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
