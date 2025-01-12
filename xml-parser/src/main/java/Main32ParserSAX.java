
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

public class Main32ParserSAX extends DefaultHandler {

    private static Map<String, String> categoryMap = new HashMap<>();
    private List<Movie> movieList = new ArrayList<>();
    private Set<String> genresDiscovered = new HashSet<>();;
    private Movie currentMovie;
    private String currentDirectorName;
    private String currentTitle;
    private Integer currentYear;
    private String currentFid;
    private List<String> currentGenres;
    private String tempVal;

    Main32ParserSAX() {
        categoryMap.put("axtn", "Action");
        categoryMap.put("act", "Action");
        categoryMap.put("actn", "Action");
        categoryMap.put("adctx", "Action");
        categoryMap.put("adct", "Action");
        categoryMap.put("advt", "Adventure");
        categoryMap.put("allegory", "Allegory");
        categoryMap.put("avant garde", "Avant Garde");
        categoryMap.put("avga", "Avant Garde");
        categoryMap.put("biob", "Biography");
        categoryMap.put("biog", "Biography");
        categoryMap.put("biop", "Biography");
        categoryMap.put("biopp", "Biography");
        categoryMap.put("black", "Black");
        categoryMap.put("camp", "Camp");
        categoryMap.put("cart", "Cartoon");
        categoryMap.put("ca", "Comedy");
        categoryMap.put("comd", "Comedy");
        categoryMap.put("cops-and-robbers", "Cops and Robbers");
        categoryMap.put("cnr", "Cops and Robbers");
        categoryMap.put("cnrb", "Cops and Robbers");
        categoryMap.put("cmr", "Cops and Robbers");
        categoryMap.put("crim", "Crime");
        categoryMap.put("ctxxx", "Other");
        categoryMap.put("ctxx", "Other");
        categoryMap.put("disa", "Disaster");
        categoryMap.put("dist", "Disaster");
        categoryMap.put("docu", "Documentary");
        categoryMap.put("dram", "Drama");
        categoryMap.put("drama", "Drama");
        categoryMap.put("dramd", "Drama");
        categoryMap.put("dram>", "Drama");
        categoryMap.put("dramn", "Drama");
        categoryMap.put("dram.actn", "Action Drama");
        categoryMap.put("duco", "Documentary");
        categoryMap.put("epic", "Epic");
        categoryMap.put("expm", "Experimental");
        categoryMap.put("fant", "Fantasy");
        categoryMap.put("fantasy", "Fantasy");
        categoryMap.put("faml", "Family");
        categoryMap.put("family", "Family");
        categoryMap.put("h", "Horror");
        categoryMap.put("hor", "Horror");
        categoryMap.put("horr", "Horror");
        categoryMap.put("homo", "Horror");
        categoryMap.put("hist", "History");
        categoryMap.put("history", "History");
        categoryMap.put("horror", "Horror");
        categoryMap.put("kinky", "Kinky");
        categoryMap.put("musc", "Musical");
        categoryMap.put("music", "Musical");
        categoryMap.put("muscl", "Musical");
        categoryMap.put("musical", "Musical");
        categoryMap.put("myst", "Mystery");
        categoryMap.put("mystery", "Mystery");
        categoryMap.put("noir", "Noir");
        categoryMap.put("porn", "Pornography");
        categoryMap.put("psy", "Psychological");
        categoryMap.put("psych dram", "Drama");
        categoryMap.put("romt", "Romance");
        categoryMap.put("romt actn", "Romantic Action");
        categoryMap.put("romt comd", "Romantic Comedy");
        categoryMap.put("romt dram", "Romantic Drama");
        categoryMap.put("romtx", "Romance");
        categoryMap.put("road", "Romance");
        categoryMap.put("rfp", "Romance");
        categoryMap.put("sctn", "Sci-Fi");
        categoryMap.put("scat", "Sci-Fi");
        categoryMap.put("scfi", "Sci-Fi");
        categoryMap.put("sci-fi", "Sci-Fi");
        categoryMap.put("sfx", "Sci-Fi");
        categoryMap.put("surr", "Surreal");
        categoryMap.put("surreal", "Surreal");
        categoryMap.put("stage musical", "Musical");
        categoryMap.put("susp", "Suspense");
        categoryMap.put("tv", "TV-Show");
        categoryMap.put("tvs", "TV-Series");
        categoryMap.put("tvm", "TV-Miniseries");
        categoryMap.put("tvmini", "TV-Miniseries");
        categoryMap.put("verite", "Documentary");
        categoryMap.put("viol", "Violence");
        categoryMap.put("west", "Western");
        categoryMap.put("west1", "Western");
        categoryMap.put("weird", "Other");
        categoryMap.put("wrestling", "Wrestling");


    }


    public List<Movie> getMovies() { return movieList; }
    public Set<String> getGenres() { return genresDiscovered; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("director")) {
            // Capture the director's name
            currentDirectorName = "";
        } else if (qName.equalsIgnoreCase("film")) {
            // Prepare for a new film
            currentTitle = "";
            currentYear = 0;
            currentFid = "";
        } else if (qName.equalsIgnoreCase("cats")) {
            currentGenres = new ArrayList<>();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("dirname")) {
            currentDirectorName = tempVal;
        } else if (qName.equalsIgnoreCase("t")) {
            currentTitle = tempVal;
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                if (tempVal != null && !tempVal.trim().isEmpty()) {
                    currentYear = Integer.parseInt(tempVal);
                } else {
                    currentYear = null;
                }
            } catch (NumberFormatException e) {
                currentYear = null;
            }

        } else if (qName.equalsIgnoreCase("fid")) {
            currentFid = tempVal;
        } else if (qName.equalsIgnoreCase("cat")) {
            tempVal = tempVal.toLowerCase().trim();
            String genre = categoryMap.getOrDefault(tempVal, "Other");
            currentGenres.add(genre);
            genresDiscovered.add(genre);
        }else if (qName.equalsIgnoreCase("film")) {
            if (currentFid != null && !currentFid.isEmpty()) {
                currentMovie = new Movie(currentTitle, currentDirectorName, currentFid, currentYear, currentGenres);
                movieList.add(currentMovie);
            }
        }

    }

//    public void runExample() {
//        parseDocument();
//        printData();
//    }
//
//    private void parseDocument() {
//
//        //get a factory
//        SAXParserFactory spf = SAXParserFactory.newInstance();
//        try {
//
//            //get a new instance of parser
//            SAXParser sp = spf.newSAXParser();
//
//            //parse the file and also register this class for call backs
//            sp.parse("mains243.xml", this);
//
//        } catch (SAXException se) {
//            se.printStackTrace();
//        } catch (ParserConfigurationException pce) {
//            pce.printStackTrace();
//        } catch (IOException ie) {
//            ie.printStackTrace();
//        }
//    }

//    private void printData() {
//
//        System.out.println("No of Movies '" + movieList.size() + "'.");
//
//        Iterator<Movie> it = movieList.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
//    }

//    public static void main(String[] args) {
//        SAXParserExample spe = new SAXParserExample();
//        populateCategoryMap();
//        spe.runExample();
//    }

}
