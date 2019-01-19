import org.sqlite.JDBC;

import java.sql.*;

public class Authentification {
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(JDBC.PREFIX + "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createUsersTable();
    }

    public static boolean register(String user, String password) {
        final String sql = "INSERT INTO  users(user, password) VALUES(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setString(1, user);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("test");
        register("user1", "12345");
    }

    private static void createUsersTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "	id integer PRIMARY KEY UNIQUE,\n"
                + "	user TEXT NOT NULL ,\n"
                + " password TEXT NOT NULL\n"
                + ");";
        System.out.println(sql);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
