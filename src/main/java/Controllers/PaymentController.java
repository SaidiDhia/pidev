package Controllers;

import Entites.DeliveryAddress;
import Services.CartService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import test.MainFx;

import java.awt.Desktop;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static Services.ProductService.CURRENT_USER_ID;

public class PaymentController {

    // 🔑 Replace with your Stripe test key
    private static final String STRIPE_SECRET_KEY = "sk_test_51T3MZvE9gq7T0ndukMZbiahEcX3bPMTdKFDUYnjkdetZpGNRJlLrerMMKp5AAWFLspm7GoxL50tUh7WgTPNGHFOE00JpbjRV5t";
    private static final String STRIPE_BASE_URL   = "https://api.stripe.com/v1";

    private static final int POLL_INTERVAL_MS = 3_000;
    private static final int POLL_TIMEOUT_MS  = 300_000;

    @FXML private Label     amountLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Label     firstNameError;
    @FXML private Label     lastNameError;
    @FXML private Label     emailError;
    @FXML private Button    payButton;
    @FXML private VBox      waitingBox;
    @FXML private Label     waitingLabel;

    private double          totalAmount     = 0.0;
    private String          customerName    = "";
    private String          deliveryAddress = "";
    private DeliveryAddress deliveryAddressObj;
    private volatile boolean pollingActive  = false;

    private CartService cartService = new CartService();

    // Called from LivraisonController
    public void setOrderInfo(double total, String name, String address, DeliveryAddress da) {
        this.totalAmount        = total;
        this.customerName       = name;
        this.deliveryAddress    = address;
        this.deliveryAddressObj = da;
        if (amountLabel != null)
            amountLabel.setText(String.format("%.3f TND", total));
        String[] parts = name.trim().split(" ", 2);
        if (firstNameField != null) {
            firstNameField.setText(parts.length > 0 ? parts[0] : "");
            lastNameField.setText(parts.length > 1 ? parts[1] : "");
        }
    }

    @FXML public void initialize() {
        if (amountLabel != null)
            amountLabel.setText(String.format("%.3f TND", totalAmount));

        // Real-time validation
        if (firstNameField != null) {
            firstNameField.focusedProperty().addListener((o, was, now) -> { if (!now) validateFirstName(); });
            lastNameField.focusedProperty().addListener((o, was, now)  -> { if (!now) validateLastName(); });
            emailField.focusedProperty().addListener((o, was, now)     -> { if (!now) validateEmail(); });
        }
    }

    // ─── Pay button ───────────────────────────────────────────────────────────

    @FXML
    private void initiateStripePayment() {
        boolean ok = validateFirstName() & validateLastName() & validateEmail();
        if (!ok) return;

        payButton.setDisable(true);
        payButton.setText("⏳  Connecting to Stripe...");

        String name        = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String email       = emailField.getText().trim();
        long   amountCents = Math.round(totalAmount * 100);

        new Thread(() -> {
            try {
                StripeSession session = createCheckoutSession(amountCents, email, name);
                Platform.runLater(() -> {
                    if (session == null || session.url == null) {
                        payButton.setDisable(false);
                        payButton.setText("💳  Pay with Stripe");
                        showAlert(Alert.AlertType.ERROR, "Stripe Error",
                            "Could not create payment session.\nCheck your sk_test_ key.");
                        return;
                    }
                    openInBrowser(session.url);
                    payButton.setVisible(false);
                    payButton.setManaged(false);
                    waitingBox.setVisible(true);
                    waitingBox.setManaged(true);
                    startPolling(session.id);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    payButton.setDisable(false);
                    payButton.setText("💳  Pay with Stripe");
                    showAlert(Alert.AlertType.ERROR, "Network Error", e.getMessage());
                });
            }
        }).start();
    }

    // ─── Stripe API ───────────────────────────────────────────────────────────

    private StripeSession createCheckoutSession(long amountCents, String email, String name)
            throws Exception {
        URL url = new URL(STRIPE_BASE_URL + "/checkout/sessions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((STRIPE_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8)));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(15_000);

        String body = "payment_method_types[0]=card"
            + "&mode=payment"
            + "&customer_email=" + urlEncode(email)
            + "&line_items[0][price_data][currency]=usd"
            + "&line_items[0][price_data][unit_amount]=" + amountCents
            + "&line_items[0][price_data][product_data][name]=" + urlEncode("WonderLust Order")
            + "&line_items[0][price_data][product_data][description]=" + urlEncode("Delivery: " + deliveryAddress)
            + "&line_items[0][quantity]=1"
            + "&success_url=https://example.com/success"
            + "&cancel_url=https://example.com/cancel";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        String response = readFull(conn);
        System.out.println("Stripe session: " + response.substring(0, Math.min(200, response.length())));

        String id  = extractStr(response, "id");
        String sUrl = extractStr(response, "url");
        return (id != null && sUrl != null) ? new StripeSession(id, sUrl) : null;
    }

    private void startPolling(String sessionId) {
        pollingActive = true;
        long startTime = System.currentTimeMillis();
        new Thread(() -> {
            while (pollingActive) {
                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                    if (System.currentTimeMillis() - startTime > POLL_TIMEOUT_MS) {
                        pollingActive = false;
                        Platform.runLater(() -> { resetPayButton();
                            showAlert(Alert.AlertType.ERROR, "Timeout", "Payment not completed. Try again."); });
                        return;
                    }

                    String[] fields      = getSessionFields(sessionId);
                    String status        = fields[0];
                    String paymentStatus = fields[1];
                    int elapsed          = (int)((System.currentTimeMillis() - startTime) / 1000);

                    Platform.runLater(() -> {
                        if (waitingLabel != null)
                            waitingLabel.setText("⏳  Waiting for payment... (" + elapsed + "s)");
                    });

                    if ("paid".equalsIgnoreCase(paymentStatus) || "complete".equalsIgnoreCase(status)) {
                        pollingActive = false;
                        Platform.runLater(this::finalizeOrder);
                        return;
                    }
                    if ("expired".equalsIgnoreCase(status)) {
                        pollingActive = false;
                        Platform.runLater(() -> { resetPayButton();
                            showAlert(Alert.AlertType.ERROR, "Expired", "Session expired. Please try again."); });
                        return;
                    }
                } catch (InterruptedException e) {
                    pollingActive = false; return;
                } catch (Exception e) {
                    System.err.println("Poll error: " + e.getMessage());
                }
            }
        }).start();
    }

    private String[] getSessionFields(String sessionId) throws Exception {
        URL url = new URL(STRIPE_BASE_URL + "/checkout/sessions/" + sessionId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((STRIPE_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8)));
        conn.setConnectTimeout(10_000); conn.setReadTimeout(10_000);
        String r = readFull(conn);
        return new String[]{
            nvl(extractStr(r, "status"),         "open"),
            nvl(extractStr(r, "payment_status"), "unpaid")
        };
    }

    // ─── Finalize ─────────────────────────────────────────────────────────────

    private void finalizeOrder() {
        boolean success = cartService.buyCartOnline(CURRENT_USER_ID, deliveryAddressObj);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Order Confirmed! 🎉",
                "✅ Payment confirmed by Stripe!\n\n" +
                "📦 Delivery to: " + deliveryAddress + "\n" +
                "👤 Customer: " + customerName + "\n\n" +
                "Thank you for your purchase!");
            MainFx.setCenter("/fxml/FactureList.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                "Payment confirmed but order could not be saved.\nContact support.");
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private boolean validateFirstName() {
        boolean ok = !firstNameField.getText().trim().isEmpty();
        setFieldError(firstNameField, firstNameError, "⚠  First name is required", !ok);
        return ok;
    }

    private boolean validateLastName() {
        boolean ok = !lastNameField.getText().trim().isEmpty();
        setFieldError(lastNameField, lastNameError, "⚠  Last name is required", !ok);
        return ok;
    }

    private boolean validateEmail() {
        String v  = emailField.getText().trim();
        boolean ok = v.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
        setFieldError(emailField, emailError, "⚠  Enter a valid email address", !ok);
        return ok;
    }

    private void setFieldError(TextField f, Label err, String msg, boolean hasError) {
        err.setText(msg); err.setVisible(hasError); err.setManaged(hasError);
        f.setStyle(hasError
            ? "-fx-background-radius:8;-fx-border-color:#E53935;-fx-border-radius:8;-fx-padding:10;-fx-font-size:13px;"
            : "-fx-background-radius:8;-fx-border-color:#43A047;-fx-border-radius:8;-fx-padding:10;-fx-font-size:13px;"
        );
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Desktop.getDesktop().browse(new URI(url));
            else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win"))
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                else if (os.contains("mac"))
                    Runtime.getRuntime().exec(new String[]{"open", url});
                else
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.INFORMATION, "Payment Link", "Open in browser:\n\n" + url);
        }
    }

    private void resetPayButton() {
        waitingBox.setVisible(false); waitingBox.setManaged(false);
        payButton.setVisible(true);   payButton.setManaged(true);
        payButton.setDisable(false);  payButton.setText("💳  Pay with Stripe");
    }

    private String readFull(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        java.io.InputStream s = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        return new String(s.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String extractStr(String json, String key) {
        String m = "\"" + key + "\""; int ki = json.indexOf(m); if (ki == -1) return null;
        int ci = json.indexOf(':', ki + m.length()); if (ci == -1) return null;
        int i = ci + 1; while (i < json.length() && json.charAt(i) == ' ') i++;
        if (i >= json.length() || json.charAt(i) != '"') return null; i++;
        StringBuilder sb = new StringBuilder();
        while (i < json.length()) { char c = json.charAt(i); if (c == '\\') { i += 2; continue; } if (c == '"') break; sb.append(c); i++; }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String urlEncode(String s) { try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; } }
    private String nvl(String s, String def) { return s != null ? s : def; }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @FXML private void goBack() { pollingActive = false; MainFx.setCenter("/fxml/Livraison.fxml"); }

    private static class StripeSession { final String id, url; StripeSession(String i, String u) { id = i; url = u; } }
}
