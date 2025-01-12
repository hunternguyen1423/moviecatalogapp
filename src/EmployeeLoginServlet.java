import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.gson.JsonObject;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        // Retrieve username and password from request
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("Received login request for username: " + username);

        try (Connection conn = DatabaseUtil.getSlaveConnection()) {
            System.out.println("Database connection established");

            // Query database for the user
            String query = "SELECT password FROM employees WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                System.out.println("User found in database");

                // Plain-text password comparison
                if (password.equals(storedPassword)) {
                    String role = "employee";
                    request.getSession().setAttribute("user", new User(username, role));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Login successful");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Invalid password");
                }
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "User does not exist");
            }

            resultSet.close();
            statement.close();
            response.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Internal server error");
        } finally {
            System.out.println("Sending JSON response: " + responseJsonObject.toString());
            out.write(responseJsonObject.toString());
            out.close();
        }
    }
}
