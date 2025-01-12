import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "singlemovieServlet", urlPatterns = "/api/movie")
public class singlemovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String movieId = request.getParameter("id");

        try (Connection conn = DatabaseUtil.getSlaveConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, m.price, " +
                    "(SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') FROM genres g " +
                    "JOIN genres_in_movies gim ON g.id = gim.genreId WHERE gim.movieId = m.id) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(s.id, ':', s.name) ORDER BY s.movie_count DESC, s.name ASC SEPARATOR ', ') FROM stars s " +
                    "JOIN stars_in_movies sim ON s.id = sim.starId WHERE sim.movieId = m.id) AS stars, " +
                    "r.rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.id = ?";
            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, movieId);

            ResultSet rs = statement.executeQuery();
            JsonObject movieJson = new JsonObject();
            if (rs.next()) {
                String genres = rs.getString("genres");
                movieJson.addProperty("id", rs.getString("id"));
                movieJson.addProperty("title", rs.getString("title"));
                movieJson.addProperty("year", rs.getInt("year"));
                movieJson.addProperty("director", rs.getString("director"));
                movieJson.addProperty("price", rs.getInt("price"));
                movieJson.addProperty("genres", genres);

                String[] starsArray = rs.getString("stars").split(", ");
                JsonArray starsJsonArray = new JsonArray();
                for (String starData : starsArray) {
                    String[] starInfo = starData.split(":");
                    JsonObject starJson = new JsonObject();
                    starJson.addProperty("id", starInfo[0]);
                    starJson.addProperty("name", starInfo[1]);
                    starsJsonArray.add(starJson);
                }
                movieJson.add("stars", starsJsonArray);
                movieJson.addProperty("rating", rs.getFloat("rating"));
            }

            rs.close();
            statement.close();

            out.write(movieJson.toString());
            request.getServletContext().log("getting single movie results");
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("addToCart".equals(action)) {
            try {
                String movieId = request.getParameter("movieId");
                int quantity = Integer.parseInt(request.getParameter("quantity"));

                Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
                if (cart == null) {
                    cart = new LinkedHashMap<>();
                }

                cart.put(movieId, cart.getOrDefault(movieId, 0) + quantity);
                session.setAttribute("cart", cart);

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                out.write(jsonResponse.toString());
            } catch (Exception e) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "failure");
                out.write(jsonResponse.toString());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        out.close();
        response.setStatus(200);
    }
}
