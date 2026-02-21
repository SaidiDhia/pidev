package Iservices;
import Entites.Product;

import java.util.List;

public interface IproductServices {
    void ajouterProduct();
    void modifierProduct();
    void supprimerProduct();
    List<Product> afficherProducts();
}
