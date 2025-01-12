import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class addemployee {

    /*
     *
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "test";
        String loginPasswd = "test";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword("classta");
        String insertEmployee = "INSERT INTO employees (email, password, fullname) VALUES ('classta@email.edu', '" + encryptedPassword + "', 'TA CS122B')";
        statement.execute(insertEmployee);

        statement.close();
        connection.close();

        System.out.println("finished");

    }

}
