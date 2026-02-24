package Controllers;

import javafx.fxml.FXML;
import test.MainFx;

public class RoleSelectionController {

    @FXML
    private void enterAsBuyer() {
        MainFx.setCenter("/fxml/BuyerHome.fxml");
    }

    @FXML
    private void enterAsSeller() {
        MainFx.setCenter("/fxml/SellerHome.fxml");
    }
}
