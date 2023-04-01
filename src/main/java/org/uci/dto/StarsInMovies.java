package org.uci.dto;

public class StarsInMovies {
    private String movieId;
    private String starName;

    private String starId;

    public StarsInMovies(){}

    StarsInMovies(String movieId, String starName, String starId){
        this.movieId = movieId;
        this.starName = starName;
        this.starId = starId;
    }

    public void setMovieId(String movieId){this.movieId = movieId;}

    public String getMovieId(){return this.movieId;}

    public void setStarName(String starName){this.starName = starName;}

    public String getStarName(){return this.starName;}

    public void setStarId(String starId){this.starId = starId;}

    public String getStarId(){return this.starId;}
}
