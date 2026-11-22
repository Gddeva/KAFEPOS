package cafe.service;

import cafe.model.CartLine;
import cafe.model.MenuItem;
import cafe.util.AppException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Keranjang belanja satu transaksi (murni logika, tanpa database). */
public class Cart {
    private final List<CartLine> lines = new ArrayList<>();

    /** Tambah qty; jika menu sudah ada, qty digabung. Melempar AppException jika melebihi stok. */
    public void add(MenuItem item, int qty) {
        if (qty < 1) throw new AppException("Jumlah minimal 1.");
        int existing = 0, idx = -1;
        for (int i = 0; i < lines.size(); i++)
            if (lines.get(i).item().id() == item.id()) { existing = lines.get(i).qty(); idx = i; }
        int newQty = existing + qty;
        if (newQty > item.stock()) throw new AppException("Stok " + item.name() + " hanya " + item.stock() + ".");
        CartLine line = new CartLine(item, newQty);
        if (idx >= 0) lines.set(idx, line); else lines.add(line);
    }

    public void remove(int menuId) { lines.removeIf(l -> l.item().id() == menuId); }
    public void clear() { lines.clear(); }
    public boolean isEmpty() { return lines.isEmpty(); }
    public List<CartLine> lines() { return Collections.unmodifiableList(lines); }
    public long total() { return lines.stream().mapToLong(CartLine::subtotal).sum(); }

    /** Untuk label kembalian di UI. Negatif berarti uang kurang. */
    public long change(long paid) { return paid - total(); }
}
