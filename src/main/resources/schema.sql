CREATE TABLE movies (
    elcinema_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE,
    type ENUM('Movie') NOT NULL DEFAULT 'Movie',
    elcinema_url VARCHAR(255) NOT NULL
);

CREATE TABLE tv_shows (
    elcinema_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE,
    type ENUM('Series', 'TV') NOT NULL,
    elcinema_url VARCHAR(255) NOT NULL,
);
