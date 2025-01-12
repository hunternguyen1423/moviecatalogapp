//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import jakarta.servlet.ServletConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//@WebServlet(name = "MoviesByGenreServlet", urlPatterns = "/api/movies-by-genre")
//public class MoviesByGenreServlet extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//    private DataSource dataSource;
//
//    public void init(ServletConfig config) {
//        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        System.out.println("moviesbygenreservlet called");
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        PrintWriter out = response.getWriter();
//
//        String genre = request.getParameter("genre");
//        System.out.println("genre request" + genre);
//        String sort = request.getParameter("sort");
//
//        // Base SQL query with a fixed limit of 20
//        String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
//                "(SELECT GROUP_CONCAT(g2.name ORDER BY g2.name ASC SEPARATOR ', ') " +
//                "FROM genres g2 " +
//                "JOIN genres_in_movies gim2 ON g2.id = gim2.genreId " +
//                "WHERE gim2.movieId = m.id " +
//                "ORDER BY g2.name ASC LIMIT 3) AS genres, " +
//                "(SELECT GROUP_CONCAT(CONCAT(limited_stars.id, ':', limited_stars.name) ORDER BY limited_stars.name ASC SEPARATOR ', ') " +
//                "FROM (SELECT s2.id, s2.name FROM stars s2 " +
//                "JOIN stars_in_movies sim2 ON s2.id = sim2.starId " +
//                "WHERE sim2.movieId = m.id " +
//                "ORDER BY s2.name ASC LIMIT 3) AS limited_stars) AS stars " +
//                "FROM movies m " +
//                "JOIN ratings r ON m.id = r.movieId " +
//                "JOIN genres_in_movies gim ON m.id = gim.movieId " +
//                "JOIN genres g ON gim.genreId = g.id " +
//                "WHERE g.name = ? ";
//
//
//                /*"//, " +
//                "(SELECT GROUP_CONCAT(g2.name ORDER BY g2.name ASC SEPARATOR ', ') " +
//                " FROM genres g2 JOIN genres_in_movies gim2 ON g2.id = gim2.genreId " +
//                " WHERE gim2.movieId = m.id ORDER BY g2.name ASC LIMIT 3) AS genres, " +
//                "(SELECT GROUP_CONCAT(s2.name ORDER BY s2.name ASC SEPARATOR ', ') " +
//                " FROM stars s2 JOIN stars_in_movies sim2 ON s2.id = sim2.starId " +
//                " WHERE sim2.movieId = m.id ORDER BY s2.name ASC LIMIT 3) AS stars, " +
//                " r.rating FROM movies m " +
//                " JOIN genres_in_movies gim ON m.id = gim.movieId " +
//                " JOIN genres g ON gim.genreId = g.id " +
//                " JOIN ratings r ON m.id = r.movieId " +
//                " WHERE g.name = ? ";*/
//
//        // Append sorting logic based on sort parameter
//        /*if (sort != null) {
//            switch (sort) {
//                case "title-asc-rating-desc":
//                    query += "ORDER BY m.title ASC, r.rating DESC ";
//                    break;
//                case "rating-desc-title-asc":
//                    query += "ORDER BY r.rating DESC, m.title ASC ";
//                    break;
//                case "title-desc-rating-asc":
//                    query += "ORDER BY m.title DESC, r.rating ASC ";
//                    break;
//                case "rating-asc-title-desc":
//                    query += "ORDER BY r.rating ASC, m.title DESC ";
//                    break;
//                default:
//                    query += "ORDER BY r.rating DESC, m.title ASC ";  // Default sorting
//            }
//        }*/
//
//        // Set a fixed limit of 20
//        query += "LIMIT 20";
//
//        try (Connection conn = dataSource.getConnection()) {
//            PreparedStatement statement = conn.prepareStatement(query);
//            statement.setString(1, genre);  // Set genre parameter
//            System.out.println("Connecting to database...");
//            ResultSet rs = statement.executeQuery();
//            JsonArray moviesArray = new JsonArray();
//
//            while (rs.next()) {
//                System.out.println("HELLO");
//                JsonObject movieJson = new JsonObject();
//                movieJson.addProperty("id", rs.getString("id"));
//                movieJson.addProperty("title", rs.getString("title"));
//                movieJson.addProperty("year", rs.getInt("year"));
//                movieJson.addProperty("director", rs.getString("director"));
//                movieJson.addProperty("rating", rs.getFloat("rating"));
//                movieJson.addProperty("genres", rs.getString("genres"));
//                movieJson.addProperty("stars", rs.getString("stars"));
//
//                moviesArray.add(movieJson);
//            }
//            System.out.println("after rs.next");
//            JsonObject result = new JsonObject();
//            result.add("movies", moviesArray);
//            out.write(result.toString());
//            response.setStatus(200);
//
//        } catch (Exception e) {
//            JsonObject errorJson = new JsonObject();
//            errorJson.addProperty("errorMessage", e.getMessage());
//            out.write(errorJson.toString());
//            response.setStatus(500);
//        } finally {
//            out.close();
//        }
//    }
//}
