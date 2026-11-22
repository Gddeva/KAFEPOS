package cafe.service;

import cafe.model.Role;
import cafe.model.User;
import cafe.repository.UserRepository;
import cafe.util.AppException;
import cafe.util.PasswordHasher;

import java.util.List;

/** Manajemen akun kasir (khusus admin). Satu cafe = 1 admin + banyak kasir. */
public class KasirService {
    private final UserRepository users;
    public KasirService(UserRepository users) { this.users = users; }

    public List<User> list() { return users.findKasirByCafe(Session.requireAdmin().cafeId()); }

    public User add(String username, String fullName, String password) {
        int cafeId = Session.requireAdmin().cafeId();
        if (AuthService.isBlank(fullName)) throw new AppException("Nama kasir wajib diisi.");
        String uname = AuthService.validateCredentials(username, password);
        if (users.existsByUsername(uname)) throw new AppException("Username sudah dipakai.");
        return users.create(new User(0, cafeId, uname, fullName.trim(), PasswordHasher.hash(password), Role.KASIR));
    }

    /** newPassword boleh null/kosong = password tidak diganti. */
    public void update(int kasirId, String fullName, String newPassword) {
        int cafeId = Session.requireAdmin().cafeId();
        User u = users.findUserById(kasirId)
                .filter(x -> x.cafeId() == cafeId && x.role() == Role.KASIR)
                .orElseThrow(() -> new AppException("Kasir tidak ditemukan."));
        if (AuthService.isBlank(fullName)) throw new AppException("Nama kasir wajib diisi.");
        String hash = u.passwordHash();
        if (!AuthService.isBlank(newPassword)) {
            if (newPassword.length() < 6) throw new AppException("Password minimal 6 karakter.");
            hash = PasswordHasher.hash(newPassword);
        }
        users.update(new User(u.id(), u.cafeId(), u.username(), fullName.trim(), hash, u.role()));
    }

    public void delete(int kasirId) { users.deleteKasir(kasirId, Session.requireAdmin().cafeId()); }
}
