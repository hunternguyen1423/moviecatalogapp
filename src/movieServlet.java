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
import java.util.concurrent.locks.ReentrantLock;

@WebServlet(name = "movieServlet", urlPatterns = "/api/movie-list")
public class movieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> cacheMap = new LinkedHashMap<String, String>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100;
        }
    };

    private final ReentrantLock cacheLock = new ReentrantLock();

    private String generateCacheKey(String genre, String sort, int page, int limit, String title, String director, int year, String star) {
        return genre + "-" + sort + "-" + page + "-" + limit + "-" + title + "-" + director + "-" + year + "-" + star;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        String sort = request.getParameter("sort");
        String genre = request.getParameter("genre");
        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String limitParam = request.getParameter("limit");
        int year = request.getParameter("year") != null ? Integer.parseInt(request.getParameter("year")) : -1;
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        int limit = limitParam != null && !limitParam.isEmpty() ? Integer.parseInt(limitParam) : 10;
        int offset = (page - 1) * limit;

        session.setAttribute("sort", sort);
        session.setAttribute("genre", genre);
        session.setAttribute("title", title);
        session.setAttribute("page", page);
        session.setAttribute("limit", limit);
        session.setAttribute("director", director);
        session.setAttribute("year", year);
        session.setAttribute("star", star);

        String cacheKey = generateCacheKey(genre, sort, page, limit, title, director, year, star);

        cacheLock.lock();
        try {
            if (cacheMap.containsKey(cacheKey)) {
                out.write(cacheMap.get(cacheKey));
                response.setStatus(200);
                return;
            }
        } finally {
            cacheLock.unlock();
        }

        String query = "SELECT m.id, m.title, m.year, m.director, r.rating, m.price, " +
                "(SELECT GROUP_CONCAT(g2.name ORDER BY g2.name ASC SEPARATOR ', ') " +
                "FROM genres g2 " +
                "JOIN genres_in_movies gim2 ON g2.id = gim2.genreId " +
                "WHERE gim2.movieId = m.id " +
                "ORDER BY g2.name ASC LIMIT 3) AS genres, " +
                "(SELECT GROUP_CONCAT(CONCAT(limited_stars.id, ':', limited_stars.name) SEPARATOR ', ') " +
                "FROM (SELECT s2.id, s2.name FROM stars s2 " +
                "JOIN stars_in_movies sim2 ON s2.id = sim2.starId " +
                "WHERE sim2.movieId = m.id " +
                "ORDER BY s2.movie_count DESC, s2.name ASC LIMIT 3) AS limited_stars) AS stars " +
                "FROM movies m " +
                "LEFT JOIN ratings r ON m.id = r.movieId " +
                "WHERE 1=1 ";

        if (genre != null && !genre.isEmpty()) {
            query += "AND m.id IN (" +
                    "SELECT m2.id " +
                    "FROM movies m2 " +
                    "JOIN genres_in_movies gim2 ON m2.id = gim2.movieId " +
                    "JOIN genres g2 ON gim2.genreId = g2.id " +
                    "WHERE g2.name = ?) ";
        }
        if (title != null && !title.isEmpty()) {
            query += "AND MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ";
        }
        if (director != null && !director.isEmpty()) {
            query += "AND LOWER(m.director) LIKE LOWER(?) ";
        }
        if (year > 0) {
            query += "AND m.year = ? ";
        }
        if (star != null && !star.isEmpty()) {
            query += "AND EXISTS (" +
                    "    SELECT 1 " +
                    "    FROM stars_in_movies sim " +
                    "    JOIN stars s ON sim.starId = s.id " +
                    "    WHERE sim.movieId = m.id " +
                    "    AND LOWER(s.name) LIKE LOWER(?)" +
                    ") ";
        }

        query += "ORDER BY r.rating DESC LIMIT ? OFFSET ?;";

        try (Connection conn = DatabaseUtil.getSlaveConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            int paramIndex = 1;

            if (genre != null && !genre.isEmpty()) {
                statement.setString(paramIndex++, genre);
            }
            if (title != null && !title.isEmpty()) {
                statement.setString(paramIndex++, title);
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(paramIndex++, director + "%");
            }
            if (year > 0) {
                statement.setInt(paramIndex++, year);
            }
            if (star != null && !star.isEmpty()) {
                statement.setString(paramIndex++, star + "%");
            }
            statement.setInt(paramIndex++, limit);
            statement.setInt(paramIndex, offset);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", rs.getString("id"));
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getInt("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("rating", rs.getFloat("rating"));
                jsonObject.addProperty("price", rs.getInt("price"));
                jsonObject.addProperty("genres", rs.getString("genres"));
                jsonObject.addProperty("stars", rs.getString("stars"));
                jsonArray.add(jsonObject);
            }

            String result = jsonArray.toString();
            cacheLock.lock();
            try {
                cacheMap.put(cacheKey, result);
            } finally {
                cacheLock.unlock();
            }
            out.write(result);
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
