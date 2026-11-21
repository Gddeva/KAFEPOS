package cafe.model;

/** Nama & harga disalin saat transaksi, supaya riwayat tetap benar walau menu diubah/dihapus. */
public record TransactionLine(int menuId, String itemName, long price, int qty, long subtotal) {}
