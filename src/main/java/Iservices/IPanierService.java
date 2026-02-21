package Iservices;

public interface IPanierService {
    void addToCart(int userId, int productId, int quantity);
    void increaseQuantity(int productId);
    void decreaseQuantity(int productId);
    void removeFromCart(int productId);
    void buyCart(int userId);
    void displayCart();
}
