package Controllers;

import Entites.DeliveryAddress;
import Services.CartService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import test.MainFx;

import static Services.ProductService.CURRENT_USER_ID;

public class LivraisonController {

    @FXML private Label totalSummaryLabel;
    @FXML private Label totalBottomLabel;

    // Address fields
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextArea  notesField;

    // Error labels
    @FXML private Label fullNameError;
    @FXML private Label phoneError;
    @FXML private Label addressError;
    @FXML private Label cityError;

    // Payment method
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton onlineRadio;
    @FXML private HBox cashCard;
    @FXML private HBox onlineCard;

    private CartService cartService = new CartService();
    private double totalAmount = 0.0;

    @FXML
    public void initialize() {
        totalAmount = cartService.calculateTotal(CURRENT_USER_ID);
        String fmt  = String.format("%.3f TND", totalAmount);
        totalSummaryLabel.setText(fmt);
        totalBottomLabel.setText(fmt);
        highlightCash();

        // Real-time validation listeners
        fullNameField.focusedProperty().addListener((o, was, now) -> { if (!now) validateFullName(); });
        phoneField.focusedProperty().addListener((o, was, now)    -> { if (!now) validatePhone(); });
        addressField.focusedProperty().addListener((o, was, now)   -> { if (!now) validateAddress(); });
        cityField.focusedProperty().addListener((o, was, now)      -> { if (!now) validateCity(); });
    }

    // ─── Payment selection ────────────────────────────────────────────────────

    @FXML private void selectCash()   { cashRadio.setSelected(true);   highlightCash(); }
    @FXML private void selectOnline() { onlineRadio.setSelected(true); highlightOnline(); }

    private void highlightCash() {
        cashCard.setStyle("-fx-background-color:#F9FBE7;-fx-background-radius:10;-fx-border-color:#CDDC39;-fx-border-radius:10;-fx-border-width:2;-fx-padding:15;-fx-cursor:hand;");
        onlineCard.setStyle("-fx-background-color:#F5F5F5;-fx-background-radius:10;-fx-border-color:#E0E0E0;-fx-border-radius:10;-fx-border-width:2;-fx-padding:15;-fx-cursor:hand;");
    }

    private void highlightOnline() {
        onlineCard.setStyle("-fx-background-color:#E8F5E9;-fx-background-radius:10;-fx-border-color:#43A047;-fx-border-radius:10;-fx-border-width:2;-fx-padding:15;-fx-cursor:hand;");
        cashCard.setStyle("-fx-background-color:#F5F5F5;-fx-background-radius:10;-fx-border-color:#E0E0E0;-fx-border-radius:10;-fx-border-width:2;-fx-padding:15;-fx-cursor:hand;");
    }

    // ─── Confirm ──────────────────────────────────────────────────────────────

    @FXML
    private void confirmOrder() {
        // Validate all fields first
        boolean ok = validateFullName() & validatePhone() & validateAddress() & validateCity();
        if (!ok) {
            showAlert(Alert.AlertType.WARNING, "Form Error",
                "Please fix the highlighted fields before continuing.");
            return;
        }

        DeliveryAddress da = buildDeliveryAddress();

        if (cashRadio.isSelected()) {
            // Cash → reserve stock, save immediately
            boolean success = cartService.buyCartCash(CURRENT_USER_ID, da);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Order Placed! 🎉",
                    "✅ Your order has been placed!\n\n" +
                    "📦 Delivery to: " + da.getAddress() + ", " + da.getCity() + "\n" +
                    "💵 Payment: Cash on Delivery\n\n" +
                    "The seller will confirm your delivery.");
                MainFx.setCenter("/fxml/FactureList.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Failed",
                    "Could not place order. Items may be out of stock.");
            }
        } else {
            // Online → go to Stripe payment page
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Payment.fxml"));
                Parent root = loader.load();
                PaymentController ctrl = loader.getController();
                ctrl.setOrderInfo(totalAmount,
                    da.getFullName(),
                    da.getAddress() + ", " + da.getCity(),
                    da);
                MainFx.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not open payment page.");
            }
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private boolean validateFullName() {
        String v = fullNameField.getText().trim();
        boolean ok = v.length() >= 3 && v.matches("[a-zA-ZÀ-ÿ\\s'-]+");
        setError(fullNameField, fullNameError,
            "⚠  Name must be at least 3 letters (letters only)", !ok);
        return ok;
    }

    private boolean validatePhone() {
        String v = phoneField.getText().trim().replaceAll("[\\s\\-\\+]", "");
        boolean ok = v.matches("\\d{8,15}");
        setError(phoneField, phoneError,
            "⚠  Enter a valid phone number (8–15 digits)", !ok);
        return ok;
    }

    private boolean validateAddress() {
        String v = addressField.getText().trim();
        boolean ok = v.length() >= 5;
        setError(addressField, addressError,
            "⚠  Address must be at least 5 characters", !ok);
        return ok;
    }

    private boolean validateCity() {
        String v = cityField.getText().trim();
        boolean ok = v.length() >= 2;
        setError(cityField, cityError,
            "⚠  Please enter your city", !ok);
        return ok;
    }

    private void setError(TextField field, Label errorLabel, String msg, boolean hasError) {
        errorLabel.setText(msg);
        errorLabel.setVisible(hasError);
        errorLabel.setManaged(hasError);
        field.setStyle(hasError
            ? "-fx-background-radius:8;-fx-border-color:#E53935;-fx-border-radius:8;-fx-padding:10;-fx-font-size:13px;"
            : "-fx-background-radius:8;-fx-border-color:#43A047;-fx-border-radius:8;-fx-padding:10;-fx-font-size:13px;"
        );
    }

    // ─── Build delivery address object ────────────────────────────────────────

    private DeliveryAddress buildDeliveryAddress() {
        DeliveryAddress da = new DeliveryAddress();
        da.setFullName(fullNameField.getText().trim());
        da.setPhone(phoneField.getText().trim());
        da.setAddress(addressField.getText().trim());
        da.setCity(cityField.getText().trim());
        da.setPostalCode(postalField.getText().trim());
        da.setNotes(notesField.getText().trim());
        return da;
    }

    @FXML private void goBack() { MainFx.setCenter("/fxml/Cart.fxml"); }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
