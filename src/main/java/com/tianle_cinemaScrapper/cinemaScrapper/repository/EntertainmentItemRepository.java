package com.tianle_cinemaScrapper.cinemaScrapper.repository;

import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EntertainmentItemRepository {
    private final JdbcTemplate jdbcTemplate;

    public EntertainmentItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //----------save data into SQL movie table-----------------
    public void saveToMovies(EntertainmentItem item){
        String sql = "INSERT INTO movies (elcinema_id, title, release_date, type, elcinema_url)" +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title=VALUES(title), release_date=VALUES(release_date), elcinema_url=VALUES(elcinema_url)";
        jdbcTemplate.update(sql,item.getElCinemaId(),item.getTitle(), item.getReleaseDate(), item.getType(), item.getElCinemaUrl());

    }

    //----------save data into SQL tvshows table-----------------
    public void saveToTvShows(EntertainmentItem item) {
        String sql = "INSERT INTO tv_shows (elcinema_id, title, release_date, type, elcinema_url) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title=VALUES(title), release_date=VALUES(release_date), elcinema_url=VALUES(elcinema_url)";

        jdbcTemplate.update(sql, item.getElCinemaId(), item.getTitle(), item.getReleaseDate(), item.getType(), item.getElCinemaUrl());
    }
}
