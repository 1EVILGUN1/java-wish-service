CREATE TABLE IF NOT EXISTS users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(512),
    last_name   VARCHAR(512),
    password    VARCHAR(512),
    birthday    DATE,
    friends_ids BIGINT ARRAY,  -- Указываем тип элементов массива
    present_ids BIGINT ARRAY,  -- Указываем тип элементов массива
    url         VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS presents
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(512),
    description TEXT,
    links       VARCHAR ARRAY,  -- Указываем тип элементов массива
    url         VARCHAR(1024),
    reserved    BOOLEAN
);