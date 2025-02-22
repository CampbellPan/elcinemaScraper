package com.tianle_cinemaScrapper.cinemaScrapper.model;

import java.util.Date;

public class EntertainmentItem {

    private String elCinemaId;
    private String title;
    private Date releaseDate;
    private String type;  // "Movie" or "Series" / "TV"
    private String elCinemaUrl;

    public String getElCinemaId() {
        return elCinemaId;
    }

    public void setElCinemaId(String elCinemaId) {
        this.elCinemaId = elCinemaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getElCinemaUrl() {
        return elCinemaUrl;
    }

    public void setElCinemaUrl(String elCinemaUrl) {
        this.elCinemaUrl = elCinemaUrl;
    }

}
