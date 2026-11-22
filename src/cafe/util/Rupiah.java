package cafe.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class Rupiah {
    private Rupiah() {}
    public static String format(long amount) {
        return "Rp " + NumberFormat.getIntegerInstance(new Locale("id", "ID")).format(amount);
    }
}
