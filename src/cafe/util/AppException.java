package cafe.util;

/** Error yang pesannya aman ditampilkan langsung ke pengguna (mis. lewat Alert). */
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
}
