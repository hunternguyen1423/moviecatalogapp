
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class Cast93ParserSAX extends DefaultHandler {

    private Map<String, Star> stars;
    private Set<String> unidentifiedStars = new HashSet<>();
    private Set<String> moviesNoStars = new HashSet<>();
    private Integer starCount;
    private String movieId;
    private String currentName;
    private String tempVal;

    Cast93ParserSAX(Map<String, Star> starsMap) {
        stars = starsMap;
    }

    public Map<String, Star> getStars() { return stars; }
    public Set<String> getUnidentifiedStars() { return unidentifiedStars; }
    public Set<String> getMoviesNoStars() { return moviesNoStars; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("f")) {
            movieId = "";
        } else if (qName.equalsIgnoreCase("a")) {
            currentName = "";
        } else if (qName.equalsIgnoreCase("filmc")) {
            starCount = 0;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("f")) {
            movieId = tempVal;
        } else if (qName.equalsIgnoreCase("a")) {
            starCount++;
            currentName = tempVal;
            if (currentName != null && !currentName.isEmpty() && stars.containsKey(currentName) ) {
                stars.get(currentName).addMovie(movieId);
            } else {
                unidentifiedStars.add(currentName);
            }
        } else if (qName.equalsIgnoreCase("filmc")) {
            if (starCount == 0) {
                moviesNoStars.add(movieId);
            }
        }

    }

}
