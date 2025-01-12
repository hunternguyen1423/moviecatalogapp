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

@WebServlet(name = "genreServlet", urlPatterns = "/api/genres")
public class genreServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String query = "SELECT id, name FROM genres ORDER BY name";

        try (Connection conn = DatabaseUtil.getSlaveConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", rs.getInt("id"));
                jsonObject.addProperty("name", rs.getString("name"));
                jsonArray.add(jsonObject);
            }

            out.write(jsonArray.toString());
            request.getServletContext().log("Fetched " + jsonArray.size() + " genres");
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
}
