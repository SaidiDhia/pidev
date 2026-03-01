package com.example.pi_dev.booking.Controllers.Host;

import com.example.pi_dev.booking.Entities.GeoPoint;
import com.example.pi_dev.booking.Entities.Place;
import com.example.pi_dev.booking.Entities.PlaceImage;
import com.example.pi_dev.booking.Services.GeoService;
import com.example.pi_dev.booking.Services.PlaceImageService;
import com.example.pi_dev.booking.Services.PlaceService;
import com.example.pi_dev.booking.Utils.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaceFormController {

    // ── Standard fields ──────────────────────────────────────────────────────
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField maxGuestsField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField categoryField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;
    @FXML
    private Button submitBtn;
    @FXML
    private Label geoStatusLabel;

    // ── Photo upload ──────────────────────────────────────────────────────────
    @FXML
    private FlowPane photosPreview;
    @FXML
    private Label photoCountLabel;

    // ── Services ──────────────────────────────────────────────────────────────
    private final PlaceService placeService = new PlaceService();
    private final PlaceImageService placeImageService = new PlaceImageService();
    private final GeoService geoService = new GeoService();

    // ── State ─────────────────────────────────────────────────────────────────
    private Place editingPlace;
    private Double pendingLat;
    private Double pendingLng;

    /** Tracks images selected in the current session (new uploads). */
    private final List<SelectedImage> selectedImages = new ArrayList<>();

    /** Already-persisted images (edit mode) */
    private final List<PlaceImage> existingImages = new ArrayList<>();

    // ── Inner DTO ─────────────────────────────────────────────────────────────
    private static class SelectedImage {
        File file;
        boolean isPrimary;

        SelectedImage(File file, boolean isPrimary) {
            this.file = file;
            this.isPrimary = isPrimary;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        safeSetText(errorLabel, "");
        safeSetText(successLabel, "");
        safeSetText(geoStatusLabel, "");
        safeSetText(photoCountLabel, "No photos selected");
    }

    /** Pre-fill form for editing an existing place. */
    public void setEditingPlace(Place p) {
        this.editingPlace = p;
        this.pendingLat = p.getLat();
        this.pendingLng = p.getLng();

        titleField.setText(p.getTitle());
        descriptionArea.setText(p.getDescription());
        priceField.setText(String.valueOf(p.getPricePerDay()));
        capacityField.setText(String.valueOf(p.getCapacity()));
        maxGuestsField.setText(String.valueOf(p.getMaxGuests()));
        addressField.setText(p.getAddress());
        cityField.setText(p.getCity());
        categoryField.setText(p.getCategory());

        if (submitBtn != null)
            submitBtn.setText("Update Listing");
        if (geoStatusLabel != null && p.getLat() != null) {
            geoStatusLabel.setText("✅ Adresse validée (lat=" + p.getLat() + ")");
            geoStatusLabel.setStyle("-fx-text-fill: #16A34A;");
        }

        // Load existing images from DB
        existingImages.clear();
        existingImages.addAll(placeImageService.getImagesForPlace(p.getId()));
        refreshPhotoPreview();
    }

    // ── Photo upload ──────────────────────────────────────────────────────────

    @FXML
    private void handleAddPhotos() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Photos");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.PNG", "*.JPG",
                        "*.JPEG"));

        List<File> files = chooser.showOpenMultipleDialog(
                photosPreview != null ? photosPreview.getScene().getWindow() : null);

        if (files == null || files.isEmpty())
            return;

        for (File f : files) {
            // First added image becomes primary by default if none selected yet
            boolean makePrimary = selectedImages.isEmpty() && existingImages.isEmpty();
            selectedImages.add(new SelectedImage(f, makePrimary));
        }

        refreshPhotoPreview();
    }

    /** Rebuilds the thumbnail preview pane from selectedImages + existingImages. */
    private void refreshPhotoPreview() {
        if (photosPreview == null)
            return;
        photosPreview.getChildren().clear();

        int totalCount = existingImages.size() + selectedImages.size();

        // Show existing (persisted) images in edit mode
        for (PlaceImage img : existingImages) {
            photosPreview.getChildren().add(buildExistingThumb(img));
        }

        // Show newly selected (not yet uploaded) images
        for (int i = 0; i < selectedImages.size(); i++) {
            photosPreview.getChildren().add(buildNewThumb(selectedImages.get(i), i));
        }

        safeSetText(photoCountLabel,
                totalCount == 0 ? "No photos selected" : totalCount + " photo(s)");
    }

    private StackPane buildNewThumb(SelectedImage si, int index) {
        StackPane pane = new StackPane();
        pane.setPrefSize(88, 80);
        pane.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-border-color: "
                + (si.isPrimary ? "#2563EB" : "#D1D5DB")
                + "; -fx-border-width: 2; -fx-border-radius: 8;");

        ImageView iv = new ImageView();
        iv.setFitWidth(84);
        iv.setFitHeight(76);
        iv.setPreserveRatio(true);
        try {
            iv.setImage(new Image(si.file.toURI().toString(), true));
        } catch (Exception ignored) {
        }

        // "⭐" overlay if primary
        Label star = new Label(si.isPrimary ? "⭐" : "");
        star.setStyle("-fx-font-size: 14px;");
        StackPane.setAlignment(star, Pos.TOP_LEFT);

        // Remove button (✕)
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: rgba(220,38,38,0.85); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-min-width: 18; -fx-min-height: 18; " +
                "-fx-padding: 0 4 0 4; -fx-font-size: 10px; -fx-cursor: hand;");
        StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);
        removeBtn.setOnAction(e -> {
            selectedImages.remove(si);
            refreshPhotoPreview();
        });

        // Click on image → toggle primary
        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseClicked(e -> {
            selectedImages.forEach(s -> s.isPrimary = false);
            existingImages.forEach(img -> {
                if (img.getPlaceId() > 0 && editingPlace != null) {
                    // will be handled separately in DB
                }
            });
            si.isPrimary = true;
            refreshPhotoPreview();
        });

        pane.getChildren().addAll(iv, star, removeBtn);

        // Tooltip hint
        Tooltip.install(pane,
                new Tooltip(si.isPrimary ? "Primary image (click to change)" : "Click image to set as primary"));
        return pane;
    }

    private StackPane buildExistingThumb(PlaceImage img) {
        StackPane pane = new StackPane();
        pane.setPrefSize(88, 80);
        pane.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-border-color: "
                + (img.isPrimary() ? "#2563EB" : "#D1D5DB")
                + "; -fx-border-width: 2; -fx-border-radius: 8;");

        ImageView iv = new ImageView();
        iv.setFitWidth(84);
        iv.setFitHeight(76);
        iv.setPreserveRatio(true);
        try {
            String url = img.getUrl();
            if (url != null && !url.isBlank()) {
                java.io.File f;
                if (url.startsWith("file:")) {
                    f = new java.io.File(java.net.URI.create(url));
                } else if (url.startsWith("http")) {
                    iv.setImage(new Image(url, true));
                    f = null;
                } else {
                    f = new java.io.File(url);
                }
                if (f != null && f.exists()) {
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                        iv.setImage(new Image(fis));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        Label star = new Label(img.isPrimary() ? "⭐" : "");
        star.setStyle("-fx-font-size: 14px;");
        StackPane.setAlignment(star, Pos.TOP_LEFT);

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: rgba(220,38,38,0.85); -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-min-width: 18; -fx-min-height: 18; " +
                "-fx-padding: 0 4 0 4; -fx-font-size: 10px; -fx-cursor: hand;");
        StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);
        removeBtn.setOnAction(e -> {
            placeImageService.deleteImage(img.getId(), img.getPlaceId());
            existingImages.remove(img);
            refreshPhotoPreview();
        });

        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseClicked(e -> {
            placeImageService.setPrimary(img.getPlaceId(), img.getId());
            existingImages.forEach(i -> i.setPrimary(false));
            img.setPrimary(true);
            refreshPhotoPreview();
        });

        pane.getChildren().addAll(iv, star, removeBtn);
        return pane;
    }

    // ── Geocoding ─────────────────────────────────────────────────────────────

    @FXML
    private void handleValidateAddress() {
        safeSetText(geoStatusLabel, "⏳ Geocoding...");
        if (geoStatusLabel != null)
            geoStatusLabel.setStyle("-fx-text-fill: #6B7280;");

        String address = addressField.getText().trim();
        String city = cityField.getText().trim();

        if (address.isEmpty() || city.isEmpty()) {
            safeSetText(geoStatusLabel, "❌ Please fill Address and City first.");
            if (geoStatusLabel != null)
                geoStatusLabel.setStyle("-fx-text-fill: #DC2626;");
            return;
        }

        new Thread(() -> {
            GeoPoint point = geoService.geocode(address, city);
            Platform.runLater(() -> {
                if (point != null) {
                    pendingLat = point.getLat();
                    pendingLng = point.getLng();
                    safeSetText(geoStatusLabel, "✅ Adresse validée");
                    if (geoStatusLabel != null)
                        geoStatusLabel.setStyle("-fx-text-fill: #16A34A;");
                } else {
                    pendingLat = null;
                    pendingLng = null;
                    safeSetText(geoStatusLabel, "❌ Adresse introuvable");
                    if (geoStatusLabel != null)
                        geoStatusLabel.setStyle("-fx-text-fill: #DC2626;");
                }
            });
        }).start();
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @FXML
    private void handleSubmit() {
        safeSetText(errorLabel, "");
        safeSetText(successLabel, "");

        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String priceStr = priceField.getText().trim();
        String capStr = capacityField.getText().trim();
        String maxGStr = maxGuestsField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String category = categoryField.getText().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty() || capStr.isEmpty()
                || maxGStr.isEmpty() || address.isEmpty() || city.isEmpty() || category.isEmpty()) {
            safeSetText(errorLabel, "All fields are required.");
            return;
        }

        double price;
        int capacity, maxGuests;
        try {
            price = Double.parseDouble(priceStr);
            capacity = Integer.parseInt(capStr);
            maxGuests = Integer.parseInt(maxGStr);
        } catch (NumberFormatException e) {
            safeSetText(errorLabel, "Price, Capacity and Max Guests must be numbers.");
            return;
        }

        if (editingPlace != null) {
            // ── UPDATE mode ──────────────────────────────────────────────────
            editingPlace.setTitle(title);
            editingPlace.setDescription(desc);
            editingPlace.setPricePerDay(price);
            editingPlace.setCapacity(capacity);
            editingPlace.setMaxGuests(maxGuests);
            editingPlace.setAddress(address);
            editingPlace.setCity(city);
            editingPlace.setCategory(category);
            editingPlace.setLat(pendingLat);
            editingPlace.setLng(pendingLng);
            placeService.modifierPlace(editingPlace);

            // Upload any newly selected images
            uploadSelectedImages(editingPlace.getId());
            safeSetText(successLabel, "Place updated successfully!");

        } else {
            // ── CREATE mode ──────────────────────────────────────────────────
            Place p = new Place();
            p.setHostId(Session.currentUserId);
            p.setTitle(title);
            p.setDescription(desc);
            p.setPricePerDay(price);
            p.setCapacity(capacity);
            p.setMaxGuests(maxGuests);
            p.setAddress(address);
            p.setCity(city);
            p.setCategory(category);
            p.setStatus(Place.Status.PENDING);
            p.setLat(pendingLat);
            p.setLng(pendingLng);

            int newPlaceId = placeService.ajouterPlace(p); // returns generated ID

            // Upload images after place is created (Strategy A)
            if (!selectedImages.isEmpty()) {
                try {
                    uploadSelectedImages(newPlaceId);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.WARNING,
                            "Place créée mais erreur lors de l'upload des images :\n" + ex.getMessage());
                }
            }

            safeSetText(successLabel, "Place submitted for approval! 🎉");
            clearForm();
        }
    }

    /**
     * Registers selected images in place_images using the ORIGINAL file URI.
     * No file copy — the file already exists on the same machine so JavaFX can load
     * it directly.
     */
    private void uploadSelectedImages(int placeId) {
        if (selectedImages.isEmpty())
            return;

        // Ensure exactly one primary
        boolean anyPrimary = selectedImages.stream().anyMatch(s -> s.isPrimary);
        if (!anyPrimary)
            selectedImages.get(0).isPrimary = true;

        int successCount = 0;
        for (int i = 0; i < selectedImages.size(); i++) {
            SelectedImage si = selectedImages.get(i);
            try {
                // Store the original file URI — file already exists on this machine
                String fileUri = si.file.toURI().toString();
                System.out
                        .println("DEBUG addImage placeId=" + placeId + " uri=" + fileUri + " primary=" + si.isPrimary);
                placeImageService.addImage(placeId, fileUri, i, si.isPrimary);
                successCount++;
            } catch (RuntimeException e) {
                System.err.println("Warn: addImage DB error: " + e.getMessage());
                showAlert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Erreur DB image #" + (i + 1) + ":\n" +
                                "Avez-vous bien exécuté migration_place_images.sql ?\n\n" + e.getMessage());
            }
        }

        selectedImages.clear();
        System.out.println("Upload terminé : " + successCount + " image(s) pour placeId=" + placeId);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @FXML
    private void handleCancel() {
        clearForm();
        editingPlace = null;
        pendingLat = null;
        pendingLng = null;
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        priceField.clear();
        capacityField.clear();
        maxGuestsField.clear();
        addressField.clear();
        cityField.clear();
        categoryField.clear();
        safeSetText(errorLabel, "");
        safeSetText(successLabel, "");
        safeSetText(geoStatusLabel, "");
        pendingLat = null;
        pendingLng = null;
        selectedImages.clear();
        existingImages.clear();
        refreshPhotoPreview();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void safeSetText(Label label, String text) {
        if (label != null)
            label.setText(text);
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String message) {
        javafx.application.Platform.runLater(
                () -> new javafx.scene.control.Alert(type, message, javafx.scene.control.ButtonType.OK).showAndWait());
    }
}
