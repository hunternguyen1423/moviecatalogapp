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

@WebServlet(name = "loginServlet", urlPatterns = "/api/login")
public class loginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        /*try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed");
            response.setStatus(400);
            out.write(responseJsonObject.toString());
            out.close();
            return;
        }*/

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = DatabaseUtil.getMasterConnection()) {
            String query = "SELECT password FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                // Compare the plain text password directly (Insecure)
                if (password.equals(storedPassword)) {
                    String role = "customer";
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
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Internal server error");
        } finally {
            out.write(responseJsonObject.toString());
            out.close();
        }
    }
}
