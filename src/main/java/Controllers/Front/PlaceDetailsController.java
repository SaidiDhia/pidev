package Controllers.Front;

import Entities.Place;
import Entities.Review;
import Services.PlaceService;
import Services.ReviewService;
import Utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PlaceDetailsController {

    @FXML
    private ImageView placeImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label maxGuestsLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Button reviewBtn;
    @FXML
    private WebView mapWebView;
    @FXML
    private Label mapStatusLabel;

    // Reviews section
    @FXML
    private VBox reviewsContainer;

    private Place currentPlace;
    private final PlaceService placeService = new PlaceService();
    private final ReviewService reviewService = new ReviewService();

    public void setPlace(Place place) {
        this.currentPlace = place;
        populateUI(place);
        loadMap(place);
        loadReviewState(place);
        loadReviews(place);
    }

    public void setPlaceById(int placeId) {
        Place p = placeService.getPlaceById(placeId);
        if (p != null)
            setPlace(p);
    }

    // ─── UI Population ────────────────────────────────────────────────────────

    private void populateUI(Place p) {
        titleLabel.setText(p.getTitle());
        cityLabel.setText("📍 " + p.getCity());
        categoryLabel.setText(p.getCategory());
        priceLabel.setText(String.format("$%.2f / day", p.getPricePerDay()));
        capacityLabel.setText(p.getCapacity() + " rooms");
        maxGuestsLabel.setText(p.getMaxGuests() + " guests max");
        addressLabel.setText(p.getAddress() + ", " + p.getCity());
        descriptionLabel.setText(p.getDescription());
        if (statusLabel != null)
            statusLabel.setText(p.getStatus().name());

        if (ratingLabel != null) {
            if (p.getAvgRating() != null && p.getReviewsCount() > 0) {
                ratingLabel.setText(String.format("★ %.1f / 5  (%d reviews)", p.getAvgRating(), p.getReviewsCount()));
            } else {
                ratingLabel.setText("No reviews yet");
            }
        }

        try {
            if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                placeImage.setImage(new Image(p.getImageUrl(), true));
            }
        } catch (Exception ignored) {
        }
    }

    // ─── Reviews list ─────────────────────────────────────────────────────────

    private void loadReviews(Place p) {
        if (reviewsContainer == null)
            return;
        reviewsContainer.getChildren().clear();

        List<Review> reviews;
        try {
            reviews = reviewService.getReviewsForPlace(p.getId());
        } catch (Exception e) {
            reviewsContainer.getChildren().add(
                    makeLabel("Could not load reviews.", "#6B7280", false));
            return;
        }

        if (reviews.isEmpty()) {
            reviewsContainer.getChildren().add(
                    makeLabel("No reviews yet. Be the first to review this place!", "#6B7280", false));
            return;
        }

        for (Review r : reviews) {
            reviewsContainer.getChildren().add(buildReviewCard(r));
        }
    }

    /** Build a card node for a single review. */
    private VBox buildReviewCard(Review r) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 10; " +
                        "-fx-background-radius: 10; -fx-padding: 14 18 14 18;");

        // Row 1: stars + user id
        HBox topRow = new HBox(10);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        String stars = "★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
        Label starsLbl = makeLabel(stars, "#F59E0B", true);
        starsLbl.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label userLbl = makeLabel("User #" + r.getUserId(), "#9CA3AF", false);
        topRow.getChildren().addAll(starsLbl, userLbl);
        card.getChildren().add(topRow);

        // Row 2: comment
        if (r.getComment() != null && !r.getComment().isBlank()) {
            Label commentLbl = makeLabel(r.getComment(), "#374151", false);
            commentLbl.setWrapText(true);
            card.getChildren().add(commentLbl);
        }

        // Row 3: AI insights (only if available)
        if (r.getSentiment() != null || r.getAiSummary() != null) {
            VBox aiBox = new VBox(4);
            aiBox.setStyle("-fx-background-color: #F0F9FF; -fx-background-radius: 8; " +
                    "-fx-border-color: #BAE6FD; -fx-border-width: 1; -fx-border-radius: 8; " +
                    "-fx-padding: 8 12 8 12;");

            Label aiTitle = makeLabel("🤖 AI Insights", "#0369A1", true);
            aiTitle.setStyle("-fx-text-fill: #0369A1; -fx-font-size: 12px; -fx-font-weight: bold;");
            aiBox.getChildren().add(aiTitle);

            if (r.getSentiment() != null) {
                String sentiment = r.getSentiment();
                String color = switch (sentiment) {
                    case "POSITIVE" -> "#16A34A";
                    case "NEGATIVE" -> "#DC2626";
                    default -> "#6B7280";
                };
                String icon = switch (sentiment) {
                    case "POSITIVE" -> "😊 ";
                    case "NEGATIVE" -> "😞 ";
                    default -> "😐 ";
                };
                Label sentLbl = new Label(icon + sentiment);
                sentLbl.setStyle(
                        "-fx-text-fill: white; -fx-background-color: " + color + "; " +
                                "-fx-background-radius: 10; -fx-padding: 2 10 2 10; " +
                                "-fx-font-weight: bold; -fx-font-size: 12px;");
                aiBox.getChildren().add(sentLbl);
            }

            if (r.getAiSummary() != null && !r.getAiSummary().isBlank()) {
                Label sumLbl = makeLabel("📝 " + r.getAiSummary(), "#374151", false);
                sumLbl.setWrapText(true);
                sumLbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
                aiBox.getChildren().add(sumLbl);
            }

            card.getChildren().add(aiBox);
        }

        // Row 4: date
        if (r.getCreatedAt() != null) {
            String dateStr = r.getCreatedAt().toLocalDate().toString();
            Label dateLbl = makeLabel(dateStr, "#9CA3AF", false);
            dateLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
            card.getChildren().add(dateLbl);
        }

        return card;
    }

    private Label makeLabel(String text, String color, boolean bold) {
        Label l = new Label(text);
        String weight = bold ? "bold" : "normal";
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: " + weight + ";");
        return l;
    }

    // ─── Map ──────────────────────────────────────────────────────────────────

    private void loadMap(Place p) {
        if (mapWebView == null)
            return;
        if (p.getLat() != null && p.getLng() != null) {
            if (mapStatusLabel != null)
                mapStatusLabel.setText("");
            mapWebView.getEngine().loadContent(buildLeafletHtml(p.getLat(), p.getLng(), p.getTitle()));
        } else {
            if (mapStatusLabel != null)
                mapStatusLabel.setText("🗺️ Localisation non disponible — address not geocoded yet.");
            mapWebView.setVisible(false);
            mapWebView.setManaged(false);
        }
    }

    private void loadReviewState(Place p) {
        if (reviewBtn == null)
            return;
        reviewBtn.setVisible(true);
        reviewBtn.setManaged(true);
    }

    private String buildLeafletHtml(double lat, double lng, String title) {
        String safeTitle = title.replace("'", "\\'").replace("\"", "&quot;");
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='utf-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>html,body,#map{height:100%;margin:0;padding:0;}</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map').setView([" + lat + "," + lng + "], 15);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'," +
                "{attribution:'© OpenStreetMap contributors'}).addTo(map);" +
                "L.marker([" + lat + "," + lng + "]).addTo(map).bindPopup('" + safeTitle + "').openPopup();" +
                "</script></body></html>";
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAddReview() {
        if (currentPlace == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/ReviewDialog.fxml"));
            Node root = loader.load();
            ReviewDialogController ctrl = loader.getController();
            ctrl.setContext(currentPlace.getId(), Session.currentUserId);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Review — " + currentPlace.getTitle());
            dialog.setScene(new Scene((javafx.scene.Parent) root));
            dialog.showAndWait();

            // Refresh everything after dialog closes
            Place refreshed = placeService.getPlaceById(currentPlace.getId());
            if (refreshed != null) {
                currentPlace = refreshed;
                populateUI(refreshed);
                loadReviews(refreshed);
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Cannot open review dialog: " + e.getMessage(), ButtonType.OK)
                    .showAndWait();
        }
    }

    @FXML
    private void handleBookThisPlace() {
        if (currentPlace == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/BookingDialog.fxml"));
            Node root = loader.load();
            BookingDialogController ctrl = loader.getController();
            ctrl.setPlace(currentPlace);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Book: " + currentPlace.getTitle());
            dialog.setScene(new Scene((javafx.scene.Parent) root));
            dialog.showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Cannot open booking: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/PlaceBrowse.fxml"));
            Node view = loader.load();
            StackPane parent = (StackPane) titleLabel.getScene().lookup("#contentPane");
            if (parent != null)
                parent.getChildren().setAll(view);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Cannot go back: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
