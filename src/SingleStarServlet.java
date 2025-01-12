import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();

        try (Connection conn = DatabaseUtil.getSlaveConnection()) { // Use slave for reading data
            String query = "SELECT * " +
                    "FROM stars s " +
                    "LEFT JOIN stars_in_movies sm ON sm.starId = s.id " +
                    "LEFT JOIN movies m ON m.id = sm.movieId " +
                    "WHERE s.id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");

                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                JsonObject starMoviesJson = new JsonObject();
                starMoviesJson.addProperty("star_id", starId);
                starMoviesJson.addProperty("star_name", starName);
                starMoviesJson.addProperty("star_dob", starDob);
                starMoviesJson.addProperty("movie_id", movieId);
                starMoviesJson.addProperty("movie_title", movieTitle);
                starMoviesJson.addProperty("movie_year", movieYear);
                starMoviesJson.addProperty("movie_director", movieDirector);

                jsonArray.add(starMoviesJson);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
