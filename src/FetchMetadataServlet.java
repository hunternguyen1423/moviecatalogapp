import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/_dashboard/api/fetchMetadata")
public class FetchMetadataServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String schemaName = "moviedb";

        try (Connection connection = DatabaseUtil.getSlaveConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, schemaName, "%", new String[] {"TABLE"});

            Map<String, List<String>> tableData = new HashMap<>();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                ResultSet columns = metaData.getColumns(null, schemaName, tableName, "%");
                List<String> columnNames = new ArrayList<>();
                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME") + " (" + columns.getString("TYPE_NAME") + ")");
                }
                tableData.put(tableName, columnNames);
                columns.close();
            }
            tables.close();

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(tableData));
        } catch (SQLException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Error fetching metadata: " + e.getMessage() + "\"}");
        }
    }
}
