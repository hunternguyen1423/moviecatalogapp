
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

public class Actor6ParserSAX extends DefaultHandler {

    private Map<String, Star> stars = new HashMap<>();
    private List<Star> duplicateStars = new ArrayList<>();
    private Star currentStar;
    private String currentName;
    private Integer currentBirthyear;
    private String currentId;
    private String tempVal;


    public Map<String, Star> getStars() { return stars; }
    public List<Star> getDuplicateStars() { return duplicateStars; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("stagename")) {
            currentName = "";
        } else if (qName.equalsIgnoreCase("dob")) {
            currentBirthyear = 0;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("stagename")) {
            currentName = tempVal;
            int hashCode = Math.abs(tempVal.hashCode());
            currentId = "mn" + (hashCode % 10_000_000);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                if (tempVal != null && !tempVal.trim().isEmpty()) {
                    currentBirthyear = Integer.parseInt(tempVal);
                } else {
                    currentBirthyear = null;
                }
            } catch (NumberFormatException e) {
                currentBirthyear = null;
            }
        } else if (qName.equalsIgnoreCase("actor")) {
            currentStar = new Star(currentName, currentId, currentBirthyear);
            if (stars.containsKey(currentName)) {
                duplicateStars.add(currentStar);
            } else {
                stars.put(currentName, currentStar);
            }


        }

    }

}
