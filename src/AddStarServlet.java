import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

@WebServlet("/_dashboard/api/addStar")
public class AddStarServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String starName = request.getParameter("starName");
        String birthYear = request.getParameter("birthYear");

        try (Connection connection = DatabaseUtil.getMasterConnection()) {
            String sql = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int hashCode = Math.abs(starName.hashCode());
                ps.setString(1, "mn" + (hashCode % 10_000_000));
                ps.setString(2, starName);
                if (birthYear != null && !birthYear.isEmpty()) {
                    ps.setInt(3, Integer.parseInt(birthYear));
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.executeUpdate();

                response.setContentType("application/json");
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("message", "Star added successfully!");
                response.getWriter().write(jsonResponse.toString());
            } catch (SQLException e) {
                response.setContentType("application/json");
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("message", e.getMessage());
                response.getWriter().write(errorResponse.toString());
            }
        } catch (SQLException e) {
            response.setContentType("application/json");
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error adding star: " + e.getMessage());
            response.getWriter().write(errorResponse.toString());
        }
    }
}
