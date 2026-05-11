CREATE TABLE product_images(
    id      bigint UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id  bigint UNSIGNED NOT NULL,
    image_url   varchar(500)    NOT NULL,
    thumbnail   boolean         NOT NULL DEFAULT false,
    display_order   int UNSIGNED    NOT NULL DEFAULT 0,
    created_at  timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE,

    KEY idx_product_id_order (product_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
