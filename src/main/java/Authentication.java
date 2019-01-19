import java.sql.*;

public class Authentication {
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + "base.db");
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

    public static boolean login(String user, String password) {
        final String sql = "SELECT password FROM users WHERE user LIKE '" + user
                + "' AND password LIKE '" + password + "';";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                String pass = resultSet.getString(resultSet.findColumn("password"));
                if (pass.equals(password)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void createUsersTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "	id integer PRIMARY KEY UNIQUE,\n"
                + "	user TEXT NOT NULL ,\n"
                + " password TEXT NOT NULL\n"
                + ");";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
