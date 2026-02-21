package Controllers;

import Entites.Product;
import Services.CartService;
import Services.ProductService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import test.MainFx;

import java.io.File;
import java.util.List;

import static Services.ProductService.CURRENT_USER_ID;
/**
 * Controller for Available Products Page
 */
public class ProductAvailableController {

    @FXML private FlowPane productFlow;
    @FXML private Label cartBadge;

    private ProductService productService = new ProductService();
    private CartService cartService = new CartService();

    // Temporary current user (replace with real session later)



    @FXML
    public void initialize() {
        loadProducts();
        refreshBadge();
    }

    /**
     * Load all available products (not from current user)
     */
    private void loadProducts() {
        productFlow.getChildren().clear();

        List<Product> products = productService.getAllAvailableProducts(CURRENT_USER_ID);

        if (products.isEmpty()) {
            Label noProducts = new Label("No products available at the moment");
            noProducts.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            productFlow.getChildren().add(noProducts);
            return;
        }

        for (Product product : products) {
            VBox card = createProductCard(product);
            productFlow.getChildren().add(card);
        }
    }

    /**
     * Create modern product card
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("product-card");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        String imagePath = product.getImage();
        Image imageToShow;

        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                // Try to load the image
                imageToShow = new Image(imagePath, 180, 120, true, true, true);
                if (imageToShow.isError()) {
                    // If loading fails, fallback to placeholder
                    imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 180, 120, true, true, true);
                }
            } else {
                // If no path, show placeholder
                imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 180, 120, true, true, true);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 180, 120, true, true, true);
        }

        imageView.setImage(imageToShow);




        // Title
        Label titleLabel = new Label(product.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Type
        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        // Price
        Label priceLabel = new Label(String.format("%.2f DT", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getQuantity());
        stockLabel.setStyle("-fx-text-fill: " + (product.getQuantity() > 0 ? "#4CAF50" : "#F44336") + ";");

        // Quantity Spinner
        Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getQuantity(), 1);
        quantitySpinner.setPrefWidth(100);
        quantitySpinner.setEditable(true);

        // Add to Cart Button
        Button addButton = new Button("Add to Cart");
        addButton.setPrefWidth(150);
        addButton.getStyleClass().add("btn-add-cart");

        addButton.setOnAction(event -> {
            int quantity = quantitySpinner.getValue();
            boolean success = cartService.addToCart(CURRENT_USER_ID, product.getId(), quantity);

            if (success) {
                refreshBadge();
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        quantity + " x " + product.getTitle() + " added to cart!");
                quantitySpinner.getValueFactory().setValue(1); // Reset
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not add to cart. Check stock availability.");
            }
        });

        card.getChildren().addAll(imageView, titleLabel, typeLabel, priceLabel, stockLabel, quantitySpinner, addButton);

        return card;
    }

    /**
     * Refresh cart badge with item count
     */
    private void refreshBadge() {
        int itemCount = cartService.getCartItems(CURRENT_USER_ID)
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
        cartBadge.setText("🛒 " + itemCount);
    }

    /**
     * Refresh products list
     */
    @FXML
    private void refresh() {
        loadProducts();
        refreshBadge();
    }

    /**
     * Navigate to Cart page
     */
    @FXML
    private void goToCart() {
        MainFx.setCenter("/fxml/Cart.fxml");
    }

    /**
     * Navigate to Product Management
     */
    @FXML
    private void goToProductManagement() {
        MainFx.setCenter("/fxml/Product_Management.fxml");
    }

    public void goToFactureList(ActionEvent actionEvent) {
        MainFx.setCenter("/fxml/FactureList.fxml");
    }


    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}