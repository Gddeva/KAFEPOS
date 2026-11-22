package cafe.model;

/** price dalam rupiah (bilangan bulat). */
public record MenuItem(int id, int cafeId, String name, long price, int stock) {}
