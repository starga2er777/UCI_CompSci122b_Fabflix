package org.uci;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.uci.dto.Genre;
import org.uci.dto.Movie;
import org.uci.dto.Star;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieSAXParser extends DefaultHandler{
    List<Movie> newMovies;

    List<Star> newDirectors;

    List<Genre> newGenres;

    private Star tempDirector;

    private Movie tempMovie;

    private Genre tempGenre;


    private String tempInfo;

    MovieSAXParser(){
        newMovies = new ArrayList<>();
        newDirectors = new ArrayList<>();
        newGenres = new ArrayList<>();
    }


    protected void parseDocument(String filename) {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(filename, this);

        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("No of Movies '" + newMovies.size() + "'.");

        for (Movie newMovie : newMovies) {
            System.out.println(newMovie.toString());
        }
    }

    //Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempInfo = "";
        if (qName.equalsIgnoreCase("director")) {
            //create a new instance of director
            tempDirector = new Star();
        } else if (qName.equalsIgnoreCase("film")){
            tempMovie = new Movie();
            tempMovie.setDirector(tempDirector.getName());
        } else if (qName.equalsIgnoreCase("cat")){
            tempGenre = new Genre();
            tempGenre.setMovieId(tempMovie.getId());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempInfo = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("director")){
            //add new director to list
            newDirectors.add(tempDirector);
        } else if (qName.equalsIgnoreCase("dirid")){
            tempDirector.setId(tempInfo);
        } else if (qName.equalsIgnoreCase("dirname")){
            tempDirector.setName(tempInfo);
        } else if (qName.equalsIgnoreCase("film")) {
            //add new movie to the list
            newMovies.add(tempMovie);
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempInfo);
        } else if (qName.equalsIgnoreCase("year")) {
            // some dates are not precise
            try{
                tempMovie.setYear(Integer.parseInt(tempInfo.substring(0,4)));
            }catch (NumberFormatException | StringIndexOutOfBoundsException numberFormatException){
                //System.out.println(tempInfo);
            }
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempInfo);
        } else if (qName.equalsIgnoreCase("cat")){
            //set and add new genre correspond to movies
            if(Objects.equals(tempInfo, "")) tempInfo = null;
            else tempInfo = tempInfo.trim();
            tempGenre.setName(tempInfo);
            newGenres.add(tempGenre);
        }
    }

    /*public static void main(String[] args) {
        MovieSAXParser spm = new MovieSAXParser();
        //spe.runExample();
        spm.parseDocument("mains243.xml");

        spm.printData();
    }*/
}
