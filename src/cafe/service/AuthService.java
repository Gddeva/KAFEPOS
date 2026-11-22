package cafe.service;

import cafe.model.Cafe;
import cafe.model.Role;
import cafe.model.User;
import cafe.repository.CafeRepository;
import cafe.repository.UserRepository;
import cafe.util.AppException;
import cafe.util.PasswordHasher;

public class AuthService {
    private final CafeRepository cafes;
    private final UserRepository users;

    public AuthService(CafeRepository cafes, UserRepository users) { this.cafes = cafes; this.users = users; }

    /** Daftarkan cafe baru beserta satu-satunya akun admin-nya. */
    public User registerCafe(String cafeName, String adminFullName, String username, String password) {
        if (isBlank(cafeName) || isBlank(adminFullName)) throw new AppException("Nama cafe dan nama admin wajib diisi.");
        String uname = validateCredentials(username, password);
        if (users.existsByUsername(uname)) throw new AppException("Username sudah dipakai.");

        Cafe cafe = cafes.create(cafeName.trim());
        try {
            return users.create(new User(0, cafe.id(), uname, adminFullName.trim(), PasswordHasher.hash(password), Role.ADMIN));
        } catch (RuntimeException e) {
            cafes.deleteCafe(cafe.id());   // rollback manual
            throw e;
        }
    }

    public User login(String username, String password) {
        if (isBlank(username) || isBlank(password)) throw new AppException("Username dan password wajib diisi.");
        User u = users.findByUsername(username.trim().toLowerCase())
                .filter(x -> PasswordHasher.verify(password, x.passwordHash()))
                .orElseThrow(() -> new AppException("Username atau password salah."));
        Session.start(u);
        return u;
    }

    public void logout() { Session.logout(); }

    /** Dipakai juga oleh KasirService. Mengembalikan username yang sudah dinormalisasi. */
    static String validateCredentials(String username, String password) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{4,30}"))
            throw new AppException("Username 4-30 karakter (huruf, angka, underscore).");
        if (password == null || password.length() < 6) throw new AppException("Password minimal 6 karakter.");
        return username.trim().toLowerCase();
    }

    static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
