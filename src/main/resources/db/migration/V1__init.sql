CREATE TABLE auth_providers(
    code            varchar(20)     NOT NULL,
    name            varchar(50)     NOT NULL,
    description     text,
    PRIMARY KEY(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE users(
    id            bigint UNSIGNED   NOT NULL AUTO_INCREMENT,
    email         varchar(100)      NOT NULL,
    password_hash varchar(255),
    auth_provider varchar(20),
    name          varchar(100),
    created_at    datetime          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    datetime          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY   (id),
    UNIQUE KEY    email (email),
    CONSTRAINT    fk_auth_provider
        FOREIGN KEY (auth_provider)
        REFERENCES auth_providers (code)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE categories(
    id            bigint UNSIGNED   NOT NULL AUTO_INCREMENT,
    parent_id     bigint UNSIGNED,
    name          varchar(100)      NOT NULL,
    PRIMARY KEY   (id),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
        REFERENCES categories (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE products(
    id              bigint UNSIGNED NOT NULL AUTO_INCREMENT,
    seller_id       bigint UNSIGNED NOT NULL,
    category_id     bigint UNSIGNED NOT NULL,
    name            varchar(100)    NOT NULL,
    description     text,
    price           decimal(10,2)   NOT NULL CHECK (price >= 0),
    stock           int UNSIGNED    NOT NULL,
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

CREATE TABLE order_status(
    code            varchar(20)  NOT NULL,
    name            varchar(50)  NOT NULL,
    description     text,
    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE orders(
    id              bigint UNSIGNED NOT NULL AUTO_INCREMENT,
    buyer_id        bigint UNSIGNED NOT NULL,
    product_id      bigint UNSIGNED NOT NULL,
    quantity        int UNSIGNED    NOT NULL,
    unit_price      decimal(10,2)   NOT NULL CHECK (unit_price >= 0),
    total_price     decimal(10,2)   NOT NULL CHECK (total_price >= 0),
    status_code     varchar(20)     NOT NULL,
    created_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_orders_buyer
        FOREIGN KEY (buyer_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_orders_status
        FOREIGN KEY (status_code)
        REFERENCES  order_status (code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE carts(
    id          bigint UNSIGNED    NOT NULL AUTO_INCREMENT,
    user_id     bigint UNSIGNED    NOT NULL,
    product_id  bigint UNSIGNED    NOT NULL,
    quantity    int UNSIGNED       NOT NULL,
    created_at  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY (user_id, product_id),

    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_carts_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE wallets(
    id          bigint UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id     bigint UNSIGNED  NOT NULL,
    balance     decimal(10,2)    NOT NULL CHECK (balance >= 0),
    created_at  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE      (user_id),

    CONSTRAINT fk_wallets_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE account_record_type(
    code            varchar(20)     NOT NULL,
    name            varchar(50)     NOT NULL,
    description     text,

    PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE account_records(
    id            bigint UNSIGNED        NOT NULL AUTO_INCREMENT,
    wallet_id     bigint UNSIGNED        NOT NULL,
    type          varchar(20)            NOT NULL,
    amount        decimal(10,2)          NOT NULL CHECK (amount >= 0),
    created_at    timestamp              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_account_records_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_account_records_type
        FOREIGN KEY (type)
        REFERENCES account_record_type (code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE product_comments(
    id            bigint UNSIGNED     NOT NULL AUTO_INCREMENT,
    product_id    bigint UNSIGNED     NOT NULL,
    user_id       bigint UNSIGNED     NOT NULL,
    comment       text,
    rating        tinyint             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_product_comments_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_product_comments_user
        FOREIGN KEY (user_id)
        REFERENCES  users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uq_product_user
        UNIQUE (product_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

