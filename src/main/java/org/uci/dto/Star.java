package org.uci.dto;

public class Star {
    String id;
    String name;
    Integer birthYear;

    public Star(){}

    public void setId(String id){this.id = id;}

    public String getId(){return this.id;}

    public void setName(String name){this.name = name;}

    public String getName(){return this.name;}

    public void setBirthYear(Integer birthYear){this.birthYear = birthYear;}

    public Integer getBirthYear(){return this.birthYear;}
}
