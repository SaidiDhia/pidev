package Services;

import Entites.FactureProduct;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FactureProductServiceTest {

    static FactureProductService service;

    @BeforeAll
    static void setup() {
        service = new FactureProductService();
    }

    @Test
    void testAddAndGetFactureProduct() {

        FactureProduct fp = new FactureProduct();
        fp.setFactureId(3);
        fp.setProductId(22);
        fp.setProductTitle("Test Product");
        fp.setQuantity(2);
        fp.setPrice(100f);

        service.addFactureProduct(fp);

        List<FactureProduct> list = service.getProductsByFacture(3);

        boolean exists = list.stream()
                .anyMatch(p -> p.getProductTitle().equals("Test Product"));

        assertTrue(exists);
    }
}
