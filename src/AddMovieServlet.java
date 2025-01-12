import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/_dashboard/api/addMovie")
public class AddMovieServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieTitle = request.getParameter("movieTitle");
        String releaseYear = request.getParameter("releaseYear");
        String directorName = request.getParameter("directorNameForMovie");
        String starName = request.getParameter("starNameForMovie");
        String genreName = request.getParameter("genreName");
        String input = movieTitle + releaseYear + directorName;
        String gen_movie_id = "at" + Integer.toHexString(input.hashCode()).substring(0, 8);
        int hashCode = Math.abs(starName.hashCode());
        String opt_star_id = "mn" + (hashCode % 10_000_000);

        try (Connection connection = DatabaseUtil.getMasterConnection()) {
            CallableStatement stmt = connection.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?, ?)}");
            stmt.setString(1, movieTitle);
            stmt.setInt(2, Integer.parseInt(releaseYear));
            stmt.setString(3, directorName);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);
            stmt.setString(6, gen_movie_id);
            stmt.setString(7, opt_star_id);
            stmt.executeUpdate();

            response.setContentType("application/json");
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("message", "Movie added successfully!");
            response.getWriter().write(jsonResponse.toString());
        } catch (SQLException e) {
            response.setContentType("application/json");
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error adding movie: " + e.getMessage());
            response.getWriter().write(errorResponse.toString());
        }
    }
}
