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

    public static void register(String user, String password) throws AuthException{
        if (!userExists(user)) {
            final String sql = "INSERT INTO  users(user, password) VALUES(?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, user);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            throw new AuthException("Пользователь \"" + user + "\" уже существует.", user, password);
        }
    }

    public static void login(String user, String password) throws AuthException{
        final String sql = "SELECT password FROM users WHERE user='" + user
                + "';";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                String pass = resultSet.getString(resultSet.findColumn("password"));
                if (resultSet.getString(1).equals(password)) {
                    return;
                } else {
                    throw new AuthException("Указан не варный пароль.",
                            user, password);
                }
            } else {
                throw new AuthException("Пользователь с именем \"" + user + "\" незарегистрирован.",
                        user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean userExists(String user) {
        final String sql = "SELECT user FROM users WHERE user='" + user + "';";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void createUsersTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	user TEXT NOT NULL UNIQUE ,\n"
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
