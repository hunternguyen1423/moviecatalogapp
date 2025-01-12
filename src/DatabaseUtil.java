

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {

    private static DataSource masterDataSource;
    private static DataSource slaveDataSource;

    static {
        try {
            Context initContext = new InitialContext();
            masterDataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/moviedb");
            slaveDataSource = masterDataSource; //(DataSource) initContext.lookup("java:comp/env/jdbc/slaveDB");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DataSources");
        }
    }

    public static Connection getMasterConnection() throws SQLException {
        return masterDataSource.getConnection();
    }

    public static Connection getSlaveConnection() throws SQLException {
        try {
            return masterDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Slave database connection failed. Falling back to master database.");
            return getMasterConnection();
        }
    }
}
