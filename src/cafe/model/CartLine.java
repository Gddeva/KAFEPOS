package cafe.model;

public record CartLine(MenuItem item, int qty) {
    public long subtotal() { return item.price() * qty; }
}
