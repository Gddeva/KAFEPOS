package cafe.service;

import cafe.model.*;
import cafe.repository.MenuRepository;
import cafe.repository.TransactionRepository;
import cafe.util.AppException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final MenuRepository menus;
    private final TransactionRepository trxs;

    public TransactionService(MenuRepository menus, TransactionRepository trxs) { this.menus = menus; this.trxs = trxs; }

    /** Proses pembayaran. Harga & stok dibaca ulang dari database agar data keranjang yang basi tidak dipakai. */
    public Transaction checkout(Cart cart, long paid) {
        User cashier = Session.requireLogin();
        if (cart.isEmpty()) throw new AppException("Keranjang masih kosong.");

        List<TransactionLine> lines = new ArrayList<>();
        long total = 0;
        for (CartLine cl : cart.lines()) {
            MenuItem fresh = menus.findMenuById(cl.item().id(), cashier.cafeId())
                    .orElseThrow(() -> new AppException("Menu \"" + cl.item().name() + "\" sudah tidak tersedia."));
            if (fresh.stock() < cl.qty()) throw new AppException("Stok " + fresh.name() + " hanya " + fresh.stock() + ".");
            long sub = fresh.price() * cl.qty();
            lines.add(new TransactionLine(fresh.id(), fresh.name(), fresh.price(), cl.qty(), sub));
            total += sub;
        }
        if (paid < total) throw new AppException("Uang pembayaran kurang.");

        Transaction saved = trxs.saveAndReduceStock(new Transaction(0, cashier.cafeId(), cashier.id(),
                cashier.fullName(), LocalDateTime.now(), total, paid, paid - total, lines));
        cart.clear();
        return saved;
    }
}
