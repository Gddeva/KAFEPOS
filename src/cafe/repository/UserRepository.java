package cafe.repository;

import cafe.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User create(User user);                   // abaikan user.id(), kembalikan User dengan id baru
    boolean existsByUsername(String username);// username unik di seluruh sistem
    Optional<User> findByUsername(String username);
    Optional<User> findUserById(int id);
    List<User> findKasirByCafe(int cafeId);   // hanya role KASIR
    void update(User user);                   // update fullName & passwordHash berdasarkan id
    void deleteKasir(int id, int cafeId);     // WHERE id=? AND cafe_id=? AND role='KASIR'
}
