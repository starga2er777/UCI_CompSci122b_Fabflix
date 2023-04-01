package org.uci.dto;

public class Rating {
    private String movieId;
    private double ratings;

    Rating(){}
    Rating(String movieId, double ratings){
        this.movieId = movieId;
        this.ratings = ratings;
    }

    public void setMovieId(String movieId){this.movieId = movieId;}

    public String getMovieId(String movieId){return this.movieId;}

    public void setRatings(double ratings){this.ratings = ratings;}

    public double getRatings(){return this.ratings;}
}
