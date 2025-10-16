package manager;

public class UlogovaniKorisnik {
    private static String username;

    public static void setUsername(String uname) {
        username = uname;
    }

    public static String getUsername() {
        return username;
    }

    public static void logout() {
        username = null;
    }
}