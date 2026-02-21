package Services;

import Entites.Product;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    static ProductService service;
    static int idProductTest;

    @BeforeAll
    static void setup() {
        service = new ProductService();
        ProductService.CURRENT_USER_ID = 2; // important
    }

    @Test
    @Order(1)
    void testAddProduct() {

        Product p = new Product(
                "TestProduct",
                "Test Description",
                "For Sale",
                150f,
                5,
                "Camping",
                "img.jpg",
                new Date(),
                2
        );

        service.addProduct(p);

        List<Product> products = service.getAllProducts(ProductService.CURRENT_USER_ID);

        assertFalse(products.isEmpty());

        boolean exists = products.stream()
                .anyMatch(prod -> prod.getTitle().equals("TestProduct"));

        assertTrue(exists);

        idProductTest = products.get(products.size() - 1).getId();
        System.out.println("ID TEST = " + idProductTest);
    }

    @Test
    @Order(2)
    void testUpdateProduct() {

        Product p = service.getProductById(idProductTest);

        p.setTitle("UpdatedProduct");
        service.updateProduct(p);

        Product updated = service.getProductById(idProductTest);

        assertEquals("UpdatedProduct", updated.getTitle());
    }

    @Test
    @Order(3)
    void testDeleteProduct() {

        service.deleteProduct(idProductTest);

        Product deleted = service.getProductById(idProductTest);

        assertNull(deleted);
    }

    @AfterAll
    static void cleanUp() {
        if (service.getProductById(idProductTest) != null) {
            service.deleteProduct(idProductTest);
        }
    }

}
