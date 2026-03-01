package com.example.pi_dev.marketplace.Iservices;
import com.example.pi_dev.marketplace.Entites.Product;

import java.util.List;

public interface IproductServices {
    void ajouterProduct();
    void modifierProduct();
    void supprimerProduct();
    List<Product> afficherProducts();
}
