package cafe.memory;

import cafe.model.*;
import cafe.repository.*;
import cafe.util.AppException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Implementasi sementara di memori, supaya service bisa diuji sebelum MySQL siap. */
public class InMemoryStore implements CafeRepository, UserRepository, MenuRepository, TransactionRepository {
    private final Map<Integer, Cafe> cafes = new LinkedHashMap<>();
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private final Map<Integer, MenuItem> menus = new LinkedHashMap<>();
    private final List<Transaction> trxs = new ArrayList<>();
    private final AtomicInteger cafeSeq = new AtomicInteger(), userSeq = new AtomicInteger(),
            menuSeq = new AtomicInteger(), trxSeq = new AtomicInteger();

    // Cafe
    public Cafe create(String name) { Cafe c = new Cafe(cafeSeq.incrementAndGet(), name); cafes.put(c.id(), c); return c; }
    public Optional<Cafe> findCafeById(int id) { return Optional.ofNullable(cafes.get(id)); }
    public void updateName(int id, String name) { cafes.put(id, new Cafe(id, name)); }
    public void deleteCafe(int id) { cafes.remove(id); }

    // User
    public User create(User u) {
        User n = new User(userSeq.incrementAndGet(), u.cafeId(), u.username(), u.fullName(), u.passwordHash(), u.role());
        users.put(n.id(), n); return n;
    }
    public boolean existsByUsername(String username) { return findByUsername(username).isPresent(); }
    public Optional<User> findByUsername(String username) {
        return users.values().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
    }
    public Optional<User> findUserById(int id) { return Optional.ofNullable(users.get(id)); }
    public List<User> findKasirByCafe(int cafeId) {
        return users.values().stream().filter(u -> u.cafeId() == cafeId && u.role() == Role.KASIR).toList();
    }
    public void update(User u) { users.put(u.id(), u); }
    public void deleteKasir(int id, int cafeId) {
        User u = users.get(id);
        if (u != null && u.cafeId() == cafeId && u.role() == Role.KASIR) users.remove(id);
    }

    // Menu
    public MenuItem create(MenuItem m) {
        MenuItem n = new MenuItem(menuSeq.incrementAndGet(), m.cafeId(), m.name(), m.price(), m.stock());
        menus.put(n.id(), n); return n;
    }
    public List<MenuItem> findMenuByCafe(int cafeId) {
        return menus.values().stream().filter(m -> m.cafeId() == cafeId)
                .sorted(Comparator.comparing(MenuItem::name)).toList();
    }
    public Optional<MenuItem> findMenuById(int id, int cafeId) {
        return Optional.ofNullable(menus.get(id)).filter(m -> m.cafeId() == cafeId);
    }
    public void update(MenuItem m) { if (findMenuById(m.id(), m.cafeId()).isPresent()) menus.put(m.id(), m); }
    public void deleteMenu(int id, int cafeId) { findMenuById(id, cafeId).ifPresent(m -> menus.remove(id)); }

    // Transaksi
    public Transaction saveAndReduceStock(Transaction t) {
        for (TransactionLine l : t.lines()) {
            MenuItem m = findMenuById(l.menuId(), t.cafeId()).orElseThrow(() -> new AppException("Menu tidak ada: " + l.itemName()));
            if (m.stock() < l.qty()) throw new AppException("Stok tidak cukup: " + l.itemName());
        }
        for (TransactionLine l : t.lines()) {
            MenuItem m = menus.get(l.menuId());
            menus.put(m.id(), new MenuItem(m.id(), m.cafeId(), m.name(), m.price(), m.stock() - l.qty()));
        }
        Transaction n = new Transaction(trxSeq.incrementAndGet(), t.cafeId(), t.cashierId(), t.cashierName(),
                t.time(), t.total(), t.paid(), t.change(), t.lines());
        trxs.add(0, n);
        return n;
    }
    public List<Transaction> findByCafeAndPeriod(int cafeId, LocalDateTime from, LocalDateTime to) {
        return trxs.stream().filter(t -> t.cafeId() == cafeId && !t.time().isBefore(from) && t.time().isBefore(to)).toList();
    }
}
