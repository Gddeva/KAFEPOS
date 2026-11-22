package cafe.model;

/** id = 0 artinya belum disimpan; repository mengisi id saat create. */
public record User(int id, int cafeId, String username, String fullName, String passwordHash, Role role) {}
