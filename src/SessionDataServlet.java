import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.JsonObject;

@WebServlet(name = "SessionDataServlet", urlPatterns = "/api/session-data")
public class SessionDataServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        JsonObject jsonResponse = new JsonObject();
        System.out.print(String.valueOf(session.getAttribute("sort")));
        // Get session attributes
        jsonResponse.addProperty("sort", String.valueOf(session.getAttribute("sort")));
        jsonResponse.addProperty("page", String.valueOf(session.getAttribute("page")));
        jsonResponse.addProperty("limit", String.valueOf(session.getAttribute("limit")));
        jsonResponse.addProperty("genre", String.valueOf(session.getAttribute("genre")));
        jsonResponse.addProperty("title", String.valueOf(session.getAttribute("title")));
        jsonResponse.addProperty("year", String.valueOf(session.getAttribute("year")));
        jsonResponse.addProperty("director", String.valueOf(session.getAttribute("director")));
        jsonResponse.addProperty("star",  String.valueOf(session.getAttribute("star")));
        // Send response
        PrintWriter out = response.getWriter();
        out.write(jsonResponse.toString());
        out.close();
    }
}