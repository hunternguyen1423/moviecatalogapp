import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.io.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SAXParsing {

    private static final AtomicInteger duplicateCount = new AtomicInteger(0);
    private static final AtomicInteger starDuplicateCount = new AtomicInteger(0);
    private static final AtomicInteger inconsistentMovieCount = new AtomicInteger(0);
    private static final AtomicInteger unknownMovieCount = new AtomicInteger(0);
    private static final AtomicInteger insertedMovieCount = new AtomicInteger(0);
    private static final AtomicInteger insertedStarCount = new AtomicInteger(0);
    private static final AtomicInteger insertedGenreCount = new AtomicInteger(0);
    private static final AtomicInteger insertedGenreInMoviesCount = new AtomicInteger(0);
    private static final AtomicInteger insertedStarsInMoviesCount = new AtomicInteger(0);

    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // MOVIES
                Main32ParserSAX handler = new Main32ParserSAX();

                FileInputStream xmlFile = new FileInputStream("stanford-movies/mains243.xml");
                InputStreamReader reader = new InputStreamReader(xmlFile, StandardCharsets.ISO_8859_1);
                saxParser.parse(new org.xml.sax.InputSource(reader), handler);

                List<Movie> movies = handler.getMovies();
                List<Movie> validMovies = new ArrayList<>();
                Set<String> genres = handler.getGenres();

                // INSERT MOVIES
                String moviesSQLCheckExistence = "SELECT COUNT(*) FROM movies WHERE id = ?";
                String moviesSQL = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(moviesSQL);
                     PreparedStatement psCheckExistence = conn.prepareStatement(moviesSQLCheckExistence)) {
                    int batchSize = 500;
                    int count = 0;

                    List<Movie> currentBatchMovies = new ArrayList<>();

                    for (Movie movie : movies) {

                        if (isInconsistent(movie)) {
                            logToFile("Inconsistent movie found: " + movie.toString(), "inconsistent_movies.txt");
                            inconsistentMovieCount.incrementAndGet();
                            continue;
                        }

                        psCheckExistence.setString(1, movie.getId());
                        ResultSet rs = psCheckExistence.executeQuery();
                        rs.next();
                        int movieCount = rs.getInt(1);

                        if (movieCount > 0) {
                            logToFile("Duplicate entry found for movie: " + movie.toString(), "duplicate_movies.txt");
                            duplicateCount.incrementAndGet();
                        } else {
                            ps.setString(1, movie.getId());
                            ps.setString(2, movie.getTitle());
                            Integer year = movie.getYear();
                            if (year != null) {
                                ps.setInt(3, year);
                            } else {
                                ps.setInt(3, 0);
                            }
                            ps.setString(4, movie.getDirector());

                            ps.addBatch();
                            currentBatchMovies.add(movie);
                            validMovies.add(movie);
                            insertedMovieCount.incrementAndGet();
                        }

                        if (++count % batchSize == 0) {
                            try {
                                ps.executeBatch();
                                conn.commit();
                            } catch (BatchUpdateException e) {
                                handleBatchUpdateExceptionForMovies(e, currentBatchMovies);
                                conn.commit();
                            }
                            currentBatchMovies.clear();
                        }
                    }

                    try {
                        ps.executeBatch();
                        conn.commit();
                    } catch (BatchUpdateException e) {
                        handleBatchUpdateExceptionForMovies(e, currentBatchMovies);
                        conn.commit();
                    }

                    System.out.println("Total movies Inserted: " + insertedMovieCount.get());
                    System.out.println("Total duplicate movies found: " + duplicateCount.get());
                    System.out.println("Total inconsistent movies found: " + inconsistentMovieCount.get());

                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Error during batch insert: " + e.getMessage());
                }


                // INSERT GENRES
                String genresSQLCheckExistence = "SELECT COUNT(*) FROM genres WHERE name = ?";
                String genresSQL = "INSERT INTO genres (id, name) VALUES (null, ?) ";
                try (PreparedStatement ps = conn.prepareStatement(genresSQL);
                     PreparedStatement psCheckExistence = conn.prepareStatement(genresSQLCheckExistence)) {
                    for (String genre : genres) {
                        psCheckExistence.setString(1, genre);
                        ResultSet rs = psCheckExistence.executeQuery();
                        rs.next();
                        int genreCount = rs.getInt(1);

                        if (genreCount == 0) {
                            ps.setString(1, genre);
                            ps.executeUpdate();
                            insertedGenreCount.incrementAndGet();
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Error during insert: " + e.getMessage());
                }
                System.out.println("Total genres Inserted: " + insertedGenreCount.get());


                // INSERT GENRES IN MOVIES
                Map<String, Integer> genreIdMap = new HashMap<>();

                String fetchAllGenresSQL = "SELECT id, name FROM genres";
                try (PreparedStatement psFetchAllGenres = conn.prepareStatement(fetchAllGenresSQL)) {
                    ResultSet rs = psFetchAllGenres.executeQuery();
                    while (rs.next()) {
                        String name = rs.getString("name");
                        Integer id = rs.getInt("id");
                        genreIdMap.put(name, id);
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching genres: " + e.getMessage());
                }

                String genresInMoviesSQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

                try (PreparedStatement psInsertGenresInMovies = conn.prepareStatement(genresInMoviesSQL)) {
                    for (Movie movie : validMovies) {
                        for (String genre : movie.getGenres()) {
                            Integer genreId = genreIdMap.get(genre);

                            if (genreId != null) {
                                psInsertGenresInMovies.setInt(1, genreId);
                                psInsertGenresInMovies.setString(2, movie.getId());
                                try {
                                    int rowsAffected = psInsertGenresInMovies.executeUpdate();
                                    if (rowsAffected > 0) {
                                        insertedGenreInMoviesCount.incrementAndGet();
                                    }
                                } catch (SQLException e) {
                                    System.err.println("Error inserting movie ID: " + movie.getId() + ", genre ID: " + genreId);
                                    System.err.println("SQL Error: " + e.getMessage());
                                }
                            } else {
                                System.err.println("Genre ID not found for genre: " + genre);
                            }
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Error during insert into genres_in_movies: " + e.getMessage());
                }
                System.out.println("Total genres_in_movies Inserted: " + insertedGenreInMoviesCount.get());

                // STARS
                Actor6ParserSAX starHandler = new Actor6ParserSAX();

                FileInputStream starXmlFile = new FileInputStream("stanford-movies/actors63.xml");
                InputStreamReader starReader = new InputStreamReader(starXmlFile, StandardCharsets.ISO_8859_1);
                saxParser.parse(new org.xml.sax.InputSource(starReader), starHandler);

                Map<String, Star> stars = starHandler.getStars();
                // feed stars map to cast parser to fill movies
                Cast93ParserSAX castHandler = new Cast93ParserSAX(stars);

                FileInputStream castXmlFile = new FileInputStream("stanford-movies/casts124.xml");
                InputStreamReader castReader = new InputStreamReader(castXmlFile, StandardCharsets.ISO_8859_1);
                saxParser.parse(new org.xml.sax.InputSource(castReader), castHandler);

                stars = castHandler.getStars();
                Set<String> unidentifiedStars = castHandler.getUnidentifiedStars();
                Set<String> moviesNoStars = castHandler.getMoviesNoStars();
                List<Star> duplicateStars = starHandler.getDuplicateStars();

                // INSERT STARS
                String starsSQLCheckExistence = "SELECT COUNT(*) FROM stars WHERE name = ?";
                String starsSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(starsSQL);
                     PreparedStatement psCheckExistence = conn.prepareStatement(starsSQLCheckExistence)) {
                    int batchSize = 500;
                    int count = 0;

                    List<Star> currentBatchStars = new ArrayList<>();

                    for (Star star : stars.values()) {
                        psCheckExistence.setString(1, star.getName());
                        ResultSet rs = psCheckExistence.executeQuery();
                        rs.next();
                        int starCount = rs.getInt(1);

                        if (starCount > 0) {
                            logToFile("Duplicate entry found for Star: " + star.toString(), "duplicate_stars.txt");
                            starDuplicateCount.incrementAndGet();
                        } else {
                            ps.setString(1, star.getId());
                            ps.setString(2, star.getName());
                            ps.setInt(3, star.getBirthyear() == null ? 0 : star.getBirthyear());

                            ps.addBatch();
                            currentBatchStars.add(star);
                            insertedStarCount.incrementAndGet();
                        }

                        if (++count % batchSize == 0) {
                            try {
                                ps.executeBatch();
                                conn.commit();
                            } catch (BatchUpdateException e) {
                                handleBatchUpdateExceptionForStars(e, currentBatchStars);
                                conn.commit();
                            }
                            currentBatchStars.clear();
                        }
                    }

                    try {
                        ps.executeBatch();
                        conn.commit();
                    } catch (BatchUpdateException e) {
                        handleBatchUpdateExceptionForStars(e, currentBatchStars);
                        conn.commit();
                    }

                    for (Star star : duplicateStars) {
                        starDuplicateCount.incrementAndGet();
                        logToFile("Duplicate entry found for Star: " + star.toString(), "duplicate_stars.txt");
                    }

                    for (String star : unidentifiedStars) {
                        logToFile("Unknown Star Found: " + star, "unknown_stars.txt");
                    }

                    for (String movie : moviesNoStars) {
                        logToFile("No Star Movie Found: " + movie, "no_star_movies.txt");
                    }

                    System.out.println("Total stars Inserted: " + insertedStarCount.get());
                    System.out.println("Total duplicate stars: " + starDuplicateCount.get());
                    System.out.println("Total stars not found: " + unidentifiedStars.size());
                    System.out.println("Total movies no stars: " + moviesNoStars.size());
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Error during batch insert: " + e.getMessage());
                }

                // INSERT STARS IN MOVIES
                String starsInMoviesSQL = "INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (?, ?)";

                try (PreparedStatement psInsertStarsInMovies = conn.prepareStatement(starsInMoviesSQL);
                     PreparedStatement psCheckExistence = conn.prepareStatement(moviesSQLCheckExistence)) {
                    for (Star star : stars.values()) {
                        for (String movie : star.getMovies()) {
                            psCheckExistence.setString(1, movie);
                            ResultSet rs = psCheckExistence.executeQuery();
                            rs.next();
                            int movieCount = rs.getInt(1);

                            if (movieCount != 0) {
                                psInsertStarsInMovies.setString(1, star.getId());
                                psInsertStarsInMovies.setString(2, movie);
                                try {
                                    int rowsAffected = psInsertStarsInMovies.executeUpdate();
                                    if (rowsAffected > 0) {
                                        insertedStarsInMoviesCount.incrementAndGet();
                                    }
                                } catch (SQLException e) {
                                    System.err.println("SQL Error: " + e.getMessage());
                                }
                            } else {
                                unknownMovieCount.incrementAndGet();
                                logToFile("Movie not found: " + movie, "unknown_movies.txt");
                            }
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("Error during insert into stars_in_movies: " + e.getMessage());
                }
                System.out.println("Total stars_in_movies Inserted: " + insertedStarsInMoviesCount.get());
                System.out.println("Total unknown movies found: " + unknownMovieCount.get());


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isInconsistent(Movie movie) {
        return (movie.getYear() == null || movie.getYear() == 0) ||
                (movie.getTitle() == null || movie.getTitle().isEmpty()) ||
                (movie.getDirector() == null || movie.getDirector().isEmpty()) ||
                (movie.getGenres() == null || movie.getGenres().isEmpty());
    }

    private static void handleBatchUpdateExceptionForMovies(BatchUpdateException e, List<Movie> batchMovies) {
        int[] updateCounts = e.getUpdateCounts();

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                Movie failedMovie = batchMovies.get(i);
                logToFile("Duplicate entry found for movie: " + failedMovie.toString(), "duplicate_movies.txt");
                duplicateCount.incrementAndGet();
            }
        }
    }

    private static void handleBatchUpdateExceptionForStars(BatchUpdateException e, List<Star> batchStars) {
//        System.err.println(e.getMessage());
        int[] updateCounts = e.getUpdateCounts();

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                Star failedStar = batchStars.get(i);
                logToFile("Duplicate entry found for star: " + failedStar.toString(), "duplicate_stars.txt");
                starDuplicateCount.incrementAndGet();
            }
        }
    }

    private static void logToFile(String message, String textfile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textfile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
