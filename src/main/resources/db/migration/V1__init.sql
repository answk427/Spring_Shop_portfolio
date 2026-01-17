CREATE TABLE auth_providers(
    code            varchar(20)     NOT NULL,
    name            varchar(50)    NOT NULL,
    description     text,
    PRIMARY KEY(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE users(
    id            bigint        NOT NULL AUTO_INCREMENT,
    email         varchar(100)  NOT NULL,
    password_hash varchar(255),
    auth_provider varchar(20),
    name          varchar(100),
    created_at    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY   (id),
    UNIQUE KEY    email (email),
    CONSTRAINT    fk_auth_provider FOREIGN KEY (auth_provider)
        REFERENCES auth_providers (code)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE categories(
    id            bigint        NOT NULL AUTO_INCREMENT,
    parent_id     bigint,
    name          varchar(100)  NOT NULL,
    PRIMARY KEY   (id),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
        REFERENCES categories (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE products(
    id              bigint          NOT NULL AUTO_INCREMENT,
    seller_id       bigint          NOT NULL,
    category_id     bigint          NOT NULL,
    name            varchar(100)    NOT NULL,
    description     text,
    price           decimal(10,2)   NOT NULL,
    stock           int             NOT NULL,
    created_at      datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY     (id),
    CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id)
        REFERENCES  users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES  categories (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;