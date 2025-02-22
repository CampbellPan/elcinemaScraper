package com.tianle_cinemaScrapper.cinemaScrapper.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "entertainment_items")
public class EntertainmentDocument {
    //used to store data in the mongodb
    @Id
    private String elCinemaId;

    private String title;
    private String description;
    private List<String> genre;
    private List<String> director;
    private List<String> cast;

    public String getElCinemaId() {
        return elCinemaId;
    }

    public void setElCinemaId(String elCinemaId) {
        this.elCinemaId = elCinemaId;
    }

    public List<String> getDirector() {
        return director;
    }

    public void setDirector(List<String> director) {
        this.director = director;
    }

    public List<String> getCast() {
        return cast;
    }

    public void setCast(List<String> cast) {
        this.cast = cast;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
