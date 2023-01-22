CREATE TABLE product
(
    code TEXT NOT NULL
        CONSTRAINT pk_product_code PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE product_alternative_code
(
    alternative_code TEXT NOT NULL
        CONSTRAINT pk_alternative_code PRIMARY KEY,
    primary_code     TEXT NOT NULL,
    CONSTRAINT fk_product_alternative_code_primary_code FOREIGN KEY (primary_code) REFERENCES product (code)
);

CREATE INDEX ix_product_alternative_code_primary_code ON product_alternative_code (primary_code);

CREATE TABLE product_update_history
(
    product_code      TEXT        NOT NULL,
    check_date        TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_product_update_history_product_code FOREIGN KEY (product_code) REFERENCES product (code)
);

CREATE INDEX ix_product_update_history_product_code ON product_update_history (product_code);

CREATE TABLE build
(
    product_code      TEXT NOT NULL,
    build_full_number TEXT NOT NULL,
    release_date      DATE NOT NULL,

    CONSTRAINT pk_product_code_build_full_number PRIMARY KEY (product_code, build_full_number),
    CONSTRAINT fk_build_product_code FOREIGN KEY (product_code) REFERENCES product (code)
);

CREATE INDEX ix_build_release_date ON build (release_date);

CREATE TABLE build_process_events
(
    product_code      TEXT    NOT NULL,
    build_full_number TEXT    NOT NULL,
    event_number      INTEGER NOT NULL,
    data              JSONB,

    CONSTRAINT pk_product_code_build_full_number_event_number PRIMARY KEY (product_code, build_full_number, event_number),
    CONSTRAINT fk_build_product_code FOREIGN KEY (product_code) REFERENCES product (code)
);
