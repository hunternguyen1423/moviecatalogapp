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

@WebServlet(name = "checkoutServlet", urlPatterns = "/api/shopping-cart")
public class checkoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("checkoutServlet worked");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");

        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }

        JsonArray jsonArray = new JsonArray();

        if (cart.isEmpty()) {
            JsonObject emptyRow = new JsonObject();
            emptyRow.addProperty("id", "");
            emptyRow.addProperty("title", "No items in cart");
            emptyRow.addProperty("price", 0);
            emptyRow.addProperty("quantity", 0);
            jsonArray.add(emptyRow);
        } else {
            String movieKeys = "(";
            for (String key : cart.keySet()) {
                movieKeys += "'" + key + "', ";
            }

            movieKeys = movieKeys.substring(0, movieKeys.length() - 2);
            movieKeys += ")";

            System.out.println("Movie Keys: " + movieKeys);

            try (Connection conn = DatabaseUtil.getSlaveConnection()) {
                String query = "SELECT m.id, m.title, m.price FROM movies m WHERE m.id IN " + movieKeys + ";";
                PreparedStatement statement = conn.prepareStatement(query);
                System.out.println("Executing Query: " + query);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    String movieId = rs.getString("id");
                    jsonObject.addProperty("id", movieId);
                    jsonObject.addProperty("title", rs.getString("title"));
                    jsonObject.addProperty("price", rs.getInt("price"));
                    int quantity = cart.get(movieId);
                    jsonObject.addProperty("quantity", quantity);
                    jsonArray.add(jsonObject);
                }
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());
                response.setStatus(500);
                return;
            }
        }

        String result = jsonArray.toString();
        out.write(result);
        request.getServletContext().log("Returning " + jsonArray.size() + " results");
        response.setStatus(200);
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("updateCart".equals(action)) {
            try {
                String movieId = request.getParameter("movieId");
                int quantity = Integer.parseInt(request.getParameter("quantity"));

                Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
                cart.put(movieId, quantity);
                session.setAttribute("cart", cart);

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                out.write(jsonResponse.toString());
            } catch (Exception e) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "failure");
                out.write(jsonResponse.toString());
            }
        } else if ("removeCart".equals(action)) {
            try {
                String movieId = request.getParameter("movieId");
                Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
                cart.remove(movieId);
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
