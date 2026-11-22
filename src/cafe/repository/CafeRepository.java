package cafe.repository;

import cafe.model.Cafe;
import java.util.Optional;

public interface CafeRepository {
    Cafe create(String name);                 // INSERT, kembalikan Cafe dengan id baru
    Optional<Cafe> findCafeById(int id);
    void updateName(int id, String name);
    void deleteCafe(int id);                  // dipakai untuk rollback jika pendaftaran admin gagal
}
