package org.uci.dto;

//genres_in_movie but genre name instead of genre id
public class Genre {
    private String movieId;
    private String name; // different from table genre

    public Genre(){}

    Genre(String movieId, String name){
        this.movieId = movieId;
        this.name = name;
    }

    public void setMovieId(String movieId){this.movieId = movieId;}

    public String getMovieId(){return this.movieId;}

    public void setName(String name){this.name = name;}

    public String getName(){return this.name;}
}
