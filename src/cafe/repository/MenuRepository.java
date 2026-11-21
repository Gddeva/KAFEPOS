package cafe.repository;

import cafe.model.MenuItem;
import java.util.List;
import java.util.Optional;

/** Semua method WAJIB memfilter dengan cafeId agar data antar cafe tidak tercampur. */
public interface MenuRepository {
    MenuItem create(MenuItem item);
    List<MenuItem> findMenuByCafe(int cafeId);
    Optional<MenuItem> findMenuById(int id, int cafeId);
    void update(MenuItem item);               // WHERE id=? AND cafe_id=?
    void deleteMenu(int id, int cafeId);
}
