package com.example.pi_dev.marketplace.Controllers;

import javafx.fxml.FXML;
import com.example.pi_dev.marketplace.test.MainFx;

public class RoleSelectionController {

    @FXML
    private void enterAsBuyer() {
        MainFx.setCenter("/com/example/pi_dev/marketplace/fxml/BuyerHome.fxml");
    }

    @FXML
    private void enterAsSeller() {
        MainFx.setCenter("/com/example/pi_dev/marketplace/fxml/SellerHome.fxml");
    }
}
