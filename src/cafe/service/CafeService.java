package cafe.service;

import cafe.model.Cafe;
import cafe.repository.CafeRepository;
import cafe.util.AppException;

public class CafeService {
    private final CafeRepository cafes;
    public CafeService(CafeRepository cafes) { this.cafes = cafes; }

    public Cafe currentCafe() {
        return cafes.findCafeById(Session.requireLogin().cafeId()).orElseThrow(() -> new AppException("Cafe tidak ditemukan."));
    }

    public void renameCafe(String newName) {
        int cafeId = Session.requireAdmin().cafeId();
        if (AuthService.isBlank(newName)) throw new AppException("Nama cafe tidak boleh kosong.");
        cafes.updateName(cafeId, newName.trim());
    }
}
