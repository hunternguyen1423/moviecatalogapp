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
import java.util.Map;

@WebServlet(name = "placeOrder", urlPatterns = "/api/place-order")
public class placeOrder extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expDate = request.getParameter("expDate");

        HttpSession session = request.getSession();
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");

        try (Connection conn = DatabaseUtil.getMasterConnection()) {
            String creditCardQuery = "SELECT cu.id as customerId, cc.* FROM creditcards cc " +
                    "LEFT JOIN customers cu ON cc.id = cu.ccId " +
                    "WHERE cc.id = ? " +
                    "AND cc.firstName = ? " +
                    "AND cc.lastName = ? " +
                    "AND cc.expiration = ?";

            PreparedStatement statement = conn.prepareStatement(creditCardQuery);
            statement.setString(1, cardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, expDate);

            ResultSet rs = statement.executeQuery();

            String customerId = "";
            if (rs.next()) {
                customerId = rs.getString("customerId");
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("status", "fail");
                errorResponse.addProperty("message", "Invalid credit card details");
                out.write(errorResponse.toString());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String salesQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, NOW(), ?)";
            PreparedStatement salesStmt = conn.prepareStatement(salesQuery);

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String movieId = entry.getKey();
                int quantity = entry.getValue();

                salesStmt.setString(1, customerId);
                salesStmt.setString(2, movieId);
                salesStmt.setInt(3, quantity);
                salesStmt.executeUpdate();
            }

            session.removeAttribute("cart");
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "success");
            out.write(jsonResponse.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "An error occurred: " + e.getMessage());
            out.write(errorResponse.toString());
        } finally {
            out.close();
        }
    }
}
