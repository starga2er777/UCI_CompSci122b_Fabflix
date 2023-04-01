package edu.uci.ics.fabflixmobile.data.model;

import static java.lang.Math.min;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private String id;
    private String title;
    private short year;
    private String director;
    private ArrayList<String> genres;
    private ArrayList<String> stars;
    private String rating;

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Movie() {
    }

    public String getId() {
        return id;
    }

    public String getDirector() {
        return "Director: " + director;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public void setStars(ArrayList<String> stars) {
        this.stars = stars;
    }

    public String getTitle() {
        return title;
    }

    public short getYear() {
        return year;
    }

    public String getStarString(int size){
        if (size == -1)
            size = Integer.MAX_VALUE;
        StringBuilder result = new StringBuilder("Stars: ");
        for (int i = 0; i < min(size, stars.size()) - 1; ++i) {
            result.append(stars.get(i));
            result.append(", ");
        }
        result.append(stars.get(min(size, stars.size()) - 1));
        return result.toString();
    }

    public String getGenreString(int size){
        if (size == -1)
            size = Integer.MAX_VALUE;
        StringBuilder result = new StringBuilder("Genres: ");
        for (int i = 0; i < min(size, genres.size()) - 1; ++i) {
            result.append(genres.get(i));
            result.append(", ");
        }
        result.append(genres.get(min(size, genres.size()) - 1));
        return result.toString();
    }
}