package org.uci.dto;

public class Movie {

    private String id;
    private String title;
    private Integer year;
    private String director;

    public Movie(){}

    Movie(String id, String title, Integer year, String director){
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public void setId(String id){this.id = id;}

    public String getId(){return this.id;}

    public void setTitle(String title){this.title = title;}

    public String getTitle(){return this.title;}

    public void setYear(Integer year){this.year = year;}

    public Integer getYear(){return this.year;}

    public void setDirector(String director){this.director = director;}

    public String getDirector(){return this.director;}

}
