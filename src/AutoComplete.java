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

@WebServlet(name = "AutoComplete", urlPatterns = "/api/autocomplete")
public class AutoComplete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String input = request.getParameter("query");

        String query = "SELECT m.id, m.title, m.year " +
                "FROM movies m " +
                "WHERE 1=1 ";

        String[] tokens = input.split("\\s+");

        StringBuilder fullTextSearchQuery = new StringBuilder();
        for (String token : tokens) {
            fullTextSearchQuery.append("+").append(token).append("* ");
        }

        query += "AND MATCH(m.title) AGAINST('"
                + fullTextSearchQuery.toString().trim() + "' IN BOOLEAN MODE) ";
        query += "LIMIT 10;";

        try (Connection conn = DatabaseUtil.getSlaveConnection()) {
            System.out.print(query);
            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                jsonArray.add(generateJsonObject(rs.getInt("year"), rs.getString("id"), rs.getString("title")));
            }
            String result = jsonArray.toString();
            System.out.println(result);

            out.write(result);
            request.getServletContext().log("getting " + jsonArray.size() + " results");
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

    private static JsonObject generateJsonObject(Integer year, String MovieID, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title + " (" + year + ")");

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", MovieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
