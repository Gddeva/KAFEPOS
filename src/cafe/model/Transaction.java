package cafe.model;

import java.time.LocalDateTime;
import java.util.List;

public record Transaction(int id, int cafeId, int cashierId, String cashierName, LocalDateTime time,
                          long total, long paid, long change, List<TransactionLine> lines) {}
