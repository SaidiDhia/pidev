package Entites;

import java.util.HashMap;
import java.util.Map;

public class Panier {
    private int userId;
    // Map<ProductId, QuantityInCart>
    private Map<Integer, Integer> products;

    public Panier(int userId) {
        this.userId = userId;
        this.products = new HashMap<>();
    }

    public int getUserId() {
        return userId;
    }

    public Map<Integer, Integer> getProducts() {
        return products;
    }
}
