package cafe;

import cafe.memory.InMemoryStore;
import cafe.model.*;
import cafe.service.*;
import cafe.util.AppException;

import java.time.LocalDate;
import java.util.List;

/** Contoh alur pemakaian service. Jalankan untuk menguji logika tanpa UI/database. */
public class Demo {
    public static void main(String[] args) {
        InMemoryStore db = new InMemoryStore();   // nanti diganti implementasi MySQL dari teman
        AuthService auth = new AuthService(db, db);
        CafeService cafeSvc = new CafeService(db);
        KasirService kasirSvc = new KasirService(db);
        MenuService menuSvc = new MenuService(db);
        TransactionService trxSvc = new TransactionService(db, db);
        ReportService report = new ReportService(db);

        // 1. Daftar cafe + login admin
        auth.registerCafe("Kopi Senja", "Budi", "budi_admin", "rahasia123");
        auth.login("budi_admin", "rahasia123");

        // 2. Admin kelola menu, kasir, nama cafe
        menuSvc.add("Kopi Susu", 18000, 50);
        menuSvc.add("Roti Bakar", 15000, 20);
        kasirSvc.add("sari_kasir", "Sari", "kasir123");
        cafeSvc.renameCafe("Kopi Senja Bali");
        auth.logout();

        // 3. Kasir login: hanya transaksi & lihat menu
        auth.login("sari_kasir", "kasir123");
        try { menuSvc.add("Teh", 5000, 10); } catch (AppException e) { System.out.println("[Ditolak] " + e.getMessage()); }

        List<MenuItem> menu = menuSvc.list();
        Cart cart = new Cart();
        cart.add(menu.get(0), 2);   // Kopi Susu x2
        cart.add(menu.get(1), 1);   // Roti Bakar x1
        System.out.println("Total: " + cart.total() + ", kembalian jika bayar 100000: " + cart.change(100_000));

        Transaction t = trxSvc.checkout(cart, 100_000);
        System.out.println(ReceiptService.build(cafeSvc.currentCafe().name(), t));
        auth.logout();

        // 4. Admin lihat laporan
        auth.login("budi_admin", "rahasia123");
        List<Transaction> list = report.transactions(LocalDate.now(), LocalDate.now());
        System.out.println("Jumlah transaksi: " + list.size() + ", omzet: " + ReportService.totalRevenue(list));
    }
}
