package Services;

import Entites.Product;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartServiceTest {

    static CartService cartService;
    static ProductService productService;
    static int productIdTest;
    static int userId = 50;

    @BeforeAll
    static void setup() {

        cartService = new CartService();
        productService = new ProductService();
        ProductService.CURRENT_USER_ID = 50;

        Product p = new Product(
                "CartTestProduct",
                "Desc",
                "For Sale",
                200f,
                10,
                "Camping",
                "img.jpg",
                new Date(),
                userId
        );

        productService.addProduct(p);
        productIdTest = p.getId();
    }

    @Test
    @Order(1)
    void testAddToCart() {

        boolean added = cartService.addToCart(userId, productIdTest, 2);
        assertTrue(added);

        Map<?, Integer> items = cartService.getCartItems(userId);
        assertFalse(items.isEmpty());
    }

    @Test
    @Order(2)
    void testUpdateCartQuantity() {

        boolean updated = cartService.updateCartItemQuantity(userId, productIdTest, 3);
        assertTrue(updated);
    }

    @Test
    @Order(3)
    void testCalculateTotal() {

        double total = cartService.calculateTotal(userId);
        assertEquals(600.0, total); // 3 * 200
    }

    @Test
    @Order(4)
    void testBuyCart() {

        boolean bought = cartService.buyCart(userId);
        assertTrue(bought);

        assertEquals(0, cartService.getTotalItemsCount(userId));
    }
}
