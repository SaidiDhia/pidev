package Controllers;

import Entites.Product;
import Services.ProductService;
import Utils.ImageUploader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import test.MainFx;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static Services.ProductService.CURRENT_USER_ID;

public class ProductManagementController {

    @FXML
    private FlowPane productFlow;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> categoryFilter;

    private final ProductService ps = new ProductService();
    private ObservableList<Product> productList;

    @FXML
    public void initialize() {
        // --- FILTER SETUP ---
        categoryFilter.setItems(FXCollections.observableArrayList("All Categories", "camping", "nature", "beach"));
        categoryFilter.setValue("All Categories");

        typeFilter.setItems(FXCollections.observableArrayList("All Types", "For Sale", "For Rent"));
        typeFilter.setValue("All Types");

        // Load products as cards
        loadProducts();
    }

    @FXML
    private void goToAvailableProduct() {
        MainFx.setCenter("/fxml/ProductAvailable.fxml");
    }

    @FXML
    private void goToFactureList(ActionEvent actionEvent) {
        MainFx.setCenter("/fxml/FactureList.fxml");
    }

    private void loadProducts() {
        productFlow.getChildren().clear();
        List<Product> list = ps.getAllProducts(CURRENT_USER_ID);
        productList = FXCollections.observableArrayList(list);

        for (Product p : productList) {
            productFlow.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card-management");
        card.setPadding(new Insets(10));

        // --- IMAGE ---
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        String imagePath = p.getImage();
        Image imageToShow;

        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                imageToShow = new Image(imagePath, 150, 150, true, true, true);
                if (imageToShow.isError()) {
                    imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 150, 150, true, true, true);
                }
            } else {
                imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 150, 150, true, true, true);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            imageToShow = new Image("https://dummyimage.com/180x120/cccccc/666666&text=No+Photo", 150, 150, true, true, true);
        }

        imageView.setImage(imageToShow);


        // --- TITLE ---
        Label title = new Label(p.getTitle());
        title.getStyleClass().add("title");

        // --- DESCRIPTION ---
        Label desc = new Label(p.getDescription());
        desc.getStyleClass().add("description");
        desc.setWrapText(true);

        // --- PRICE ---
        Label price = new Label("Price: " + p.getPrice() + " DT");
        price.getStyleClass().add("price");

        // --- QUANTITY ---
        Label quantity = new Label("Quantity: " + p.getQuantity());

        // --- TYPE ---
        Label type = new Label("Type: " + p.getType());

        // --- CATEGORY ---
        Label category = new Label("Category: " + p.getCategory());

        // --- CREATED DATE ---
        Label createdDate = new Label("Created: " + p.getCreatedDate());

        // --- ACTION BUTTONS ---
        HBox actions = new HBox(10);
        Button edit = new Button("Edit");
        edit.getStyleClass().add("btn-edit");
        edit.setOnAction(e -> openUpdateDialog(p));

        Button delete = new Button("Delete");
        delete.getStyleClass().add("btn-delete");
        delete.setOnAction(e -> deleteProduct(p));

        actions.getChildren().addAll(edit, delete);

        // --- ADD ALL ELEMENTS TO CARD ---
        card.getChildren().addAll(imageView, title, desc, price, quantity, type, category, createdDate, actions);

        return card;
    }


    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        productFlow.getChildren().clear();

        productList.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(keyword))
                .forEach(p -> productFlow.getChildren().add(createProductCard(p)));
    }

    @FXML
    private void handleFilter() {
        String selectedCategory = categoryFilter.getValue();
        String selectedType = typeFilter.getValue();

        productFlow.getChildren().clear();

        productList.stream()
                .filter(p -> (selectedCategory.equals("All Categories") || p.getCategory().equalsIgnoreCase(selectedCategory)) &&
                        (selectedType.equals("All Types") || p.getType().equalsIgnoreCase(selectedType)))
                .forEach(p -> productFlow.getChildren().add(createProductCard(p)));
    }

    // ADD PRODUCT
    @FXML
    private void handleAdd() {

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Fill all required fields");
        dialog.setResizable(true);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // --- GRID SETUP ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        ColumnConstraints col0 = new ColumnConstraints(); // labels
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS); // inputs grow
        grid.getColumnConstraints().addAll(col0, col1);

        // --- INPUT FIELDS ---
        TextField titleField = new TextField();
        TextField descriptionField = new TextField();
        ComboBox<String> typeField = new ComboBox<>(FXCollections.observableArrayList("For Sale", "For Rent"));
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        ComboBox<String> categoryField = new ComboBox<>(FXCollections.observableArrayList("camping", "hiking", "beach"));

        // --- ERROR LABELS ---
        Label titleError = new Label();
        Label typeError = new Label();
        Label priceError = new Label();
        Label quantityError = new Label();
        Label categoryError = new Label();
        // Example for error labels
        titleError.getStyleClass().add("label-error");
        typeError.getStyleClass().add("label-error");
        priceError.getStyleClass().add("label-error");
        quantityError.getStyleClass().add("label-error");
        categoryError.getStyleClass().add("label-error");


        // --- ADD TO GRID ---
        grid.add(new Label("Title:"), 0, 0);          grid.add(titleField, 1, 0);          grid.add(titleError, 2, 0);
        grid.add(new Label("Description:"), 0, 1);    grid.add(descriptionField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);           grid.add(typeField, 1, 2);           grid.add(typeError, 2, 2);
        grid.add(new Label("Price:"), 0, 3);          grid.add(priceField, 1, 3);          grid.add(priceError, 2, 3);
        grid.add(new Label("Quantity:"), 0, 4);       grid.add(quantityField, 1, 4);       grid.add(quantityError, 2, 4);
        grid.add(new Label("Category:"), 0, 5);       grid.add(categoryField, 1, 5);       grid.add(categoryError, 2, 5);

        // --- IMAGE CHOOSER ---
        Button chooseImageBtn = new Button("Choose Image");
        Label imageLabel = new Label("No file selected");
        imageLabel.getStyleClass().add("label-image");
        File[] selectedFile = new File[1];
        chooseImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                imageLabel.setText(file.getName());
            }
        });
        grid.add(chooseImageBtn, 0, 6);
        grid.add(imageLabel, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // --- VALIDATION ON BUTTON CLICK ---
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            boolean valid = true;

            // --- TITLE ---
            if (titleField.getText().trim().isEmpty()) {
                titleError.setText("Title is required");
                valid = false;
            } else titleError.setText("");

            // --- TYPE ---
            if (typeField.getValue() == null || typeField.getValue().trim().isEmpty()) {
                typeError.setText("Type is required");
                valid = false;
            } else typeError.setText("");

            // --- PRICE ---
            try {
                float p = Float.parseFloat(priceField.getText());
                if (p < 0) { priceError.setText("Price must be ≥ 0"); valid = false; }
                else priceError.setText("");
            } catch (Exception e) { priceError.setText("Invalid price"); valid = false; }

            // --- QUANTITY ---
            try {
                int q = Integer.parseInt(quantityField.getText());
                if (q < 0) { quantityError.setText("Quantity must be ≥ 0"); valid = false; }
                else quantityError.setText("");
            } catch (Exception e) { quantityError.setText("Invalid quantity"); valid = false; }

            // --- CATEGORY ---
            if (categoryField.getValue() == null || categoryField.getValue().trim().isEmpty()) {
                categoryError.setText("Category is required");
                valid = false;
            } else categoryError.setText("");

            if (!valid) {
                event.consume(); // prevent dialog from closing if invalid
            }
        });

        // --- RESULT CONVERTER ---
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Product p = new Product();
                p.setTitle(titleField.getText());
                p.setDescription(descriptionField.getText());
                p.setType(typeField.getValue());
                p.setPrice(Float.parseFloat(priceField.getText()));
                p.setQuantity(Integer.parseInt(quantityField.getText()));
                p.setCategory(categoryField.getValue());
                p.setCreatedDate(new Date());
                if (selectedFile[0] != null) p.setImage(ImageUploader.uploadImage(selectedFile[0]));
                return p;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            ps.addProduct(p);
            loadProducts();
            showAlert("Success", "Product added successfully!");
        });
    }


    private void openUpdateDialog(Product p) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Update Product");
        dialog.setHeaderText("Update the product details below");
        dialog.setResizable(true);

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // --- GRID SETUP ---
        GridPane grid = new GridPane();
        grid.setHgap(15); // horizontal space between columns
        grid.setVgap(10); // vertical space between rows
        grid.setPadding(new Insets(20)); // outer padding

        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(25);
        ColumnConstraints colField = new ColumnConstraints();
        colField.setPercentWidth(50);
        colField.setHgrow(Priority.ALWAYS);
        ColumnConstraints colError = new ColumnConstraints();
        colError.setPercentWidth(25);
        grid.getColumnConstraints().addAll(colLabel, colField, colError);

        // --- INPUT FIELDS ---
        TextField titleField = new TextField(p.getTitle());
        TextField descriptionField = new TextField(p.getDescription());

        ComboBox<String> typeField = new ComboBox<>(FXCollections.observableArrayList("For Sale", "For Rent"));
        typeField.setValue(p.getType());

        TextField priceField = new TextField(String.valueOf(p.getPrice()));
        TextField quantityField = new TextField(String.valueOf(p.getQuantity()));

        ComboBox<String> categoryField = new ComboBox<>(FXCollections.observableArrayList("camping", "nature", "beach"));
        categoryField.setValue(p.getCategory());

        // --- ERROR LABELS ---
        Label titleError = new Label();
        Label typeError = new Label();
        Label priceError = new Label();
        Label quantityError = new Label();
        Label categoryError = new Label();
        Label changeError = new Label(); // for "no changes" error

        titleError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        typeError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        priceError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        quantityError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        categoryError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        changeError.setStyle("-fx-text-fill: red; -fx-font-size: 12; -fx-font-weight: bold;");

        // --- ADD TO GRID ---
        grid.add(new Label("Title:"), 0, 0);      grid.add(titleField, 1, 0);      grid.add(titleError, 2, 0);
        grid.add(new Label("Description:"), 0, 1);grid.add(descriptionField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);       grid.add(typeField, 1, 2);       grid.add(typeError, 2, 2);
        grid.add(new Label("Price:"), 0, 3);      grid.add(priceField, 1, 3);      grid.add(priceError, 2, 3);
        grid.add(new Label("Quantity:"), 0, 4);   grid.add(quantityField, 1, 4);   grid.add(quantityError, 2, 4);
        grid.add(new Label("Category:"), 0, 5);   grid.add(categoryField, 1, 5);   grid.add(categoryError, 2, 5);
        grid.add(changeError, 0, 6, 3, 1); // span all columns

        // --- IMAGE VIEW ---
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            imageView.setImage(new Image(p.getImage(), 100, 100, true, true));
        }

        Button chooseImageBtn = new Button("Change Image");
        File[] selectedFile = new File[1];
        chooseImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                imageView.setImage(new Image(file.toURI().toString(), 100, 100, true, true));
            }
        });

        HBox imageBox = new HBox(10, chooseImageBtn, imageView);
        imageBox.setPadding(new Insets(5, 0, 5, 0));
        grid.add(imageBox, 0, 7, 3, 1);

        dialog.getDialogPane().setContent(grid);

        // --- VALIDATION ON BUTTON CLICK ---
        Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean valid = true;

            if (titleField.getText().trim().isEmpty()) { titleError.setText("Title is required"); valid = false; } else titleError.setText("");
            if (typeField.getValue() == null || typeField.getValue().trim().isEmpty()) { typeError.setText("Type is required"); valid = false; } else typeError.setText("");
            try { float pVal = Float.parseFloat(priceField.getText()); if (pVal < 0) { priceError.setText("≥0"); valid = false; } else priceError.setText(""); } catch (Exception e) { priceError.setText("Invalid"); valid = false; }
            try { int qVal = Integer.parseInt(quantityField.getText()); if (qVal < 0) { quantityError.setText("≥0"); valid = false; } else quantityError.setText(""); } catch (Exception e) { quantityError.setText("Invalid"); valid = false; }
            if (categoryField.getValue() == null || categoryField.getValue().trim().isEmpty()) { categoryError.setText("Category is required"); valid = false; } else categoryError.setText("");

            // --- CHECK CHANGES ---
            boolean changed = !titleField.getText().equals(p.getTitle()) ||
                    !descriptionField.getText().equals(p.getDescription()) ||
                    !typeField.getValue().equals(p.getType()) ||
                    !priceField.getText().equals(String.valueOf(p.getPrice())) ||
                    !quantityField.getText().equals(String.valueOf(p.getQuantity())) ||
                    !categoryField.getValue().equals(p.getCategory()) ||
                    selectedFile[0] != null;

            if (!changed) { changeError.setText("You need to change at least one field"); valid = false; } else { changeError.setText(""); }

            if (!valid) event.consume();
        });

        // --- RESULT CONVERTER ---
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                p.setTitle(titleField.getText());
                p.setDescription(descriptionField.getText());
                p.setType(typeField.getValue());
                p.setPrice(Float.parseFloat(priceField.getText()));
                p.setQuantity(Integer.parseInt(quantityField.getText()));
                p.setCategory(categoryField.getValue());
                if (selectedFile[0] != null) p.setImage(ImageUploader.uploadImage(selectedFile[0]));
                return p;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            ps.updateProduct(updated);
            loadProducts();
            showAlert("Success", "Product updated successfully!");
        });
    }

    private void deleteProduct(Product p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete Product?");
        alert.setContentText("Are you sure you want to delete " + p.getTitle() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ps.deleteProduct(p.getId());
            loadProducts();
            showAlert("Deleted", "Product deleted successfully!");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }


    @FXML private void openOrders() { MainFx.setCenter("/fxml/OrdersManagement.fxml"); }
}
