public class AuthException extends Exception{
    private String user;
    private String password;

    public AuthException(String msg, String user, String password) {
        super(msg);
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
