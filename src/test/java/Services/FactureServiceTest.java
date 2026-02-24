package Services;

import Entites.Facture;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FactureServiceTest {

    static FactureService service;
    static int factureIdTest;

    @BeforeAll
    static void setup() {
        service = new FactureService();
    }

    @Test
    @Order(1)
    void testAddFacture() {

        Facture f = new Facture();
        f.setUserId(99);
        f.setDate(new Date());
        f.setTotal(500f);

        factureIdTest = service.addFacture(f);

        assertTrue(factureIdTest > 0);
    }

    @Test
    @Order(2)
    void testGetFactureById() {

        Facture f = service.getFactureById(factureIdTest);

        assertNotNull(f);
        assertEquals(500f, f.getTotal());
    }
}
