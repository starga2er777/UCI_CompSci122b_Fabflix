package org.uci;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.uci.dto.Star;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


public class ActorSAXParser extends DefaultHandler {

    List<Star> actors;

    Star tempActor;

    String tempInfo;

    ActorSAXParser() {
        actors = new ArrayList<>();
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

    //Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        //reset
        tempInfo = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of director
            tempActor = new Star();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempInfo = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("actor")) {
            actors.add(tempActor);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempActor.setName(tempInfo);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempActor.setBirthYear(Integer.parseInt(tempInfo));
            } catch (NumberFormatException | StringIndexOutOfBoundsException numberFormatException) {
                //System.out.println(tempInfo);
            }
        }
    }


}
