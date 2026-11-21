package cafe.service;

import cafe.model.Transaction;
import cafe.model.TransactionLine;
import cafe.util.Rupiah;

import java.time.format.DateTimeFormatter;

/** Membuat teks struk (lebar 32 kolom) yang siap ditampilkan atau dicetak. */
public final class ReceiptService {
    private static final int W = 32;
    private static final String LINE = "-".repeat(W) + "\n";
    private ReceiptService() {}

    public static String build(String cafeName, Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append(center(cafeName.toUpperCase())).append('\n').append(LINE);
        sb.append("No    : #").append(t.id()).append('\n');
        sb.append("Tgl   : ").append(t.time().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        sb.append("Kasir : ").append(t.cashierName()).append('\n').append(LINE);
        for (TransactionLine l : t.lines()) {
            sb.append(l.itemName()).append('\n');
            sb.append(row("  " + l.qty() + " x " + Rupiah.format(l.price()), Rupiah.format(l.subtotal())));
        }
        sb.append(LINE);
        sb.append(row("Total", Rupiah.format(t.total())));
        sb.append(row("Bayar", Rupiah.format(t.paid())));
        sb.append(row("Kembali", Rupiah.format(t.change())));
        sb.append(LINE).append(center("Terima kasih!")).append('\n');
        return sb.toString();
    }

    private static String row(String left, String right) {
        return left + " ".repeat(Math.max(1, W - left.length() - right.length())) + right + "\n";
    }
    private static String center(String s) { return " ".repeat(Math.max(0, (W - s.length()) / 2)) + s; }
}
