package cafe.service;

import cafe.model.Role;
import cafe.model.User;
import cafe.util.AppException;

/** Menyimpan user yang sedang login dan menjaga hak akses. */
public final class Session {
    private static User current;
    private Session() {}

    static void start(User u) { current = u; }
    public static void logout() { current = null; }
    public static boolean isLoggedIn() { return current != null; }
    public static boolean isAdmin() { return current != null && current.role() == Role.ADMIN; }
    public static User current() { return requireLogin(); }

    public static User requireLogin() {
        if (current == null) throw new AppException("Silakan login terlebih dahulu.");
        return current;
    }
    public static User requireAdmin() {
        User u = requireLogin();
        if (u.role() != Role.ADMIN) throw new AppException("Hanya admin yang boleh melakukan ini.");
        return u;
    }
}
