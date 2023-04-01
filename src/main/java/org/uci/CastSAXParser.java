package org.uci;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.uci.dto.StarsInMovies;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;



public class CastSAXParser extends DefaultHandler{
    static int cnt = 0;

    List<StarsInMovies> casts;

    StarsInMovies tempCast;

    String tempInfo;

    CastSAXParser(){
        casts = new ArrayList<>();
    }

    protected void parseDocument(String filename) {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(filename, this);

        } catch (SAXException | IOException | ParserConfigurationException se) {
            se.printStackTrace();
        }
    }

    //Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        //reset
        tempInfo = "";
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of director
            tempCast = new StarsInMovies();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempInfo = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("m")){
            casts.add(tempCast);
        } else if (qName.equalsIgnoreCase("f")){
            tempCast.setMovieId(tempInfo);
        } else if (qName.equalsIgnoreCase("a")){
            tempCast.setStarName(tempInfo);
        }
    }


}
