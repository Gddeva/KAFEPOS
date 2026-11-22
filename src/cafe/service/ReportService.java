package cafe.service;

import cafe.model.Transaction;
import cafe.repository.TransactionRepository;
import cafe.util.AppException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Laporan penjualan (khusus admin). */
public class ReportService {
    private final TransactionRepository trxs;
    public ReportService(TransactionRepository trxs) { this.trxs = trxs; }

    /** Rentang tanggal inklusif (dari s.d. sampai). */
    public List<Transaction> transactions(LocalDate from, LocalDate to) {
        int cafeId = Session.requireAdmin().cafeId();
        if (from == null || to == null || to.isBefore(from)) throw new AppException("Rentang tanggal tidak valid.");
        return trxs.findByCafeAndPeriod(cafeId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    public static long totalRevenue(List<Transaction> list) { return list.stream().mapToLong(Transaction::total).sum(); }

    /** Omzet per hari (urut tanggal) - cocok untuk grafik. */
    public static Map<LocalDate, Long> dailyRevenue(List<Transaction> list) {
        Map<LocalDate, Long> m = new TreeMap<>();
        for (Transaction t : list) m.merge(t.time().toLocalDate(), t.total(), Long::sum);
        return m;
    }
}
