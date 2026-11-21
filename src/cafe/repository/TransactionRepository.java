package cafe.repository;

import cafe.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository {
    /**
     * WAJIB atomik (satu transaksi database: setAutoCommit(false) ... commit/rollback):
     *  1. Kurangi stok tiap baris: UPDATE menu SET stok=stok-? WHERE id=? AND cafe_id=? AND stok>=?
     *     Jika 0 baris terpengaruh -> rollback dan lempar AppException("Stok tidak cukup: <nama>").
     *  2. Simpan header transaksi + semua detailnya.
     * Kembalikan Transaction dengan id baru.
     */
    Transaction saveAndReduceStock(Transaction trx);

    /** from inklusif, to eksklusif, urut terbaru dulu, lengkap dengan lines. */
    List<Transaction> findByCafeAndPeriod(int cafeId, LocalDateTime from, LocalDateTime to);
}
