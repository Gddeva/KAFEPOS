package cafe.service;

import cafe.model.MenuItem;
import cafe.repository.MenuRepository;
import cafe.util.AppException;

import java.util.List;

public class MenuService {
    private final MenuRepository menus;
    public MenuService(MenuRepository menus) { this.menus = menus; }

    /** Admin & kasir boleh melihat menu. */
    public List<MenuItem> list() { return menus.findMenuByCafe(Session.requireLogin().cafeId()); }

    public MenuItem add(String name, long price, int stock) {
        int cafeId = Session.requireAdmin().cafeId();
        validate(name, price, stock);
        return menus.create(new MenuItem(0, cafeId, name.trim(), price, stock));
    }

    public void update(int id, String name, long price, int stock) {
        int cafeId = Session.requireAdmin().cafeId();
        validate(name, price, stock);
        menus.findMenuById(id, cafeId).orElseThrow(() -> new AppException("Menu tidak ditemukan."));
        menus.update(new MenuItem(id, cafeId, name.trim(), price, stock));
    }

    public void delete(int id) { menus.deleteMenu(id, Session.requireAdmin().cafeId()); }

    private void validate(String name, long price, int stock) {
        if (AuthService.isBlank(name)) throw new AppException("Nama menu wajib diisi.");
        if (price <= 0) throw new AppException("Harga harus lebih dari 0.");
        if (stock < 0) throw new AppException("Stok tidak boleh negatif.");
    }
}
