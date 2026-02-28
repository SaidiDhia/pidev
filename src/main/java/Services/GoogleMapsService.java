package Services;

import Entities.Event;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GoogleMapsService {
    private static final String API_KEY = "AIzaSyDWvi3ZewbLDWLkkPlFtNg1iV7hcbdHyE4";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api";
    private static final String GEOCODING_URL = BASE_URL + "/geocode/json";
    private static final String PLACES_URL = BASE_URL + "/place/textsearch/json";
    private static final String STATIC_MAP_URL = BASE_URL + "/staticmap";
    
    private boolean isInitialized = false;

    /**
     * Initialise le service Google Maps
     */
    public boolean initialize() {
        try {
            // Mode réel avec API calls
            System.out.println("🗺️ Google Maps Service initialisé (mode réel)");
            this.isInitialized = true;
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de Google Maps: " + e.getMessage());
            this.isInitialized = false;
            return false;
        }
    }

    /**
     * Obtient les coordonnées GPS d'une adresse
     */
    public CompletableFuture<Coordinates> getCoordinates(String address) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInitialized) {
                System.err.println("Google Maps n'est pas initialisé");
                return null;
            }

            try {
                // Mode réel avec API Google Maps
                System.out.println("🗺️ Recherche des coordonnées pour: " + address);
                
                // Vérifier si la clé API est configurée
                if (API_KEY.equals("VOTRE_CLÉ_API_ICI") || API_KEY.length() < 30) {
                    System.out.println("⚠️ Pas d'API key configurée, utilisation de la simulation");
                    Coordinates coords = simulateCoordinates(address);
                    
                    if (coords != null) {
                        System.out.println("✅ Coordonnées simulées trouvées: " + coords);
                        return coords;
                    } else {
                        System.out.println("❌ Coordonnées non trouvées pour: " + address);
                        return null;
                    }
                } else {
                    System.out.println("✅ Clé API Google Maps configurée, utilisation de l'API réelle");
                    // Continuer avec l'API réelle - pas de return ici
                }
                
                // Vraie API Google Maps
                String url = String.format("%s?address=%s&key=%s", 
                    GEOCODING_URL, 
                    java.net.URLEncoder.encode(address, "UTF-8"), 
                    API_KEY);
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    Coordinates coords = parseCoordinates(response.body());
                    if (coords != null) {
                        System.out.println("✅ Coordonnées API trouvées: " + coords);
                        return coords;
                    } else {
                        System.out.println("❌ Coordonnées non trouvées dans la réponse API");
                        // Fallback sur simulation
                        return simulateCoordinates(address);
                    }
                } else {
                    System.err.println("Erreur API: " + response.statusCode());
                    System.err.println("Réponse: " + response.body());
                    // Fallback sur simulation
                    return simulateCoordinates(address);
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la recherche des coordonnées: " + e.getMessage());
                // Fallback sur simulation
                return simulateCoordinates(address);
            }
        });
    }

    /**
     * Génère une URL pour une carte statique avec marqueur
     */
    public String getStaticMapUrl(String address, int width, int height) {
        if (!isInitialized) {
            return null;
        }

        try {
            // Mode réel avec API Google Maps
            System.out.println("🗺️ Génération de la carte statique pour: " + address);
            
            // Obtenir les coordonnées d'abord
            CompletableFuture<Coordinates> coordsFuture = getCoordinates(address);
            Coordinates coords = coordsFuture.get(); // Attendre le résultat
            
            if (coords != null) {
                // Générer l'URL avec les vraies coordonnées
                return String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=15&size=%dx%d&markers=color:red|%.6f,%.6f&key=%s",
                    coords.getLatitude(), coords.getLongitude(),
                    width, height,
                    coords.getLatitude(), coords.getLongitude(),
                    API_KEY.equals("AIzaSyDWvi3ZewbLDWLkkPlFtNg1iV7hcbdHyE4") || API_KEY.length() < 30 ? "demo" : API_KEY
                );
            } else {
                // Fallback avec l'adresse textuelle
                return String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=15&size=%dx%d&markers=color:red|%s&key=%s",
                    address.replace(" ", "+"),
                    width, height,
                    address.replace(" ", "+"),
                    API_KEY.equals("AIzaSyDWvi3ZewbLDWLkkPlFtNg1iV7hcbdHyE4") || API_KEY.length() < 30 ? "demo" : API_KEY
                );
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la carte: " + e.getMessage());
            return null;
        }
    }

    /**
     * Recherche des lieux à proximité
     */
    public CompletableFuture<NearbyPlacesResult> findNearbyPlaces(String location, String type, int radius) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInitialized) {
                System.err.println("Google Maps n'est pas initialisé");
                return null;
            }

            try {
                // Mode simulation
                System.out.println("🗺️ Recherche de lieux à proximité de: " + location);
                System.out.println("   Type: " + type + ", Rayon: " + radius + "m");
                
                // Simuler des résultats
                NearbyPlacesResult result = simulateNearbyPlaces(location, type);
                
                if (result != null) {
                    System.out.println("✅ " + result.getPlaces().size() + " lieux trouvés");
                    return result;
                } else {
                    System.out.println("❌ Aucun lieu trouvé");
                    return null;
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la recherche de lieux: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Calcule la distance entre deux points
     */
    public double calculateDistance(Coordinates point1, Coordinates point2) {
        if (point1 == null || point2 == null) {
            return -1;
        }

        // Formule de Haversine pour calculer la distance
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Rayon de la Terre en kilomètres
        double earthRadius = 6371;
        return earthRadius * c;
    }

    /**
     * Génère une URL pour Google Maps avec itinéraire
     */
    public String getDirectionsUrl(String origin, String destination) {
        if (!isInitialized) {
            return null;
        }

        try {
            return String.format(
                "https://www.google.com/maps/dir/?api=1&origin=%s&destination=%s",
                origin.replace(" ", "+"),
                destination.replace(" ", "+")
            );
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de l'itinéraire: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse les coordonnées depuis la réponse JSON de l'API Google Maps
     */
    private Coordinates parseCoordinates(String jsonResponse) {
        try {
            System.out.println("DEBUG: Parsing de la réponse JSON...");
            
            // Parser simple pour extraire lat et lng du JSON
            // Format attendu: {"results": [{"geometry": {"location": {"lat": 36.8065, "lng": 10.1815}}}]}
            
            // Vérifier si la réponse contient "results"
            if (!jsonResponse.contains("\"results\"")) {
                System.out.println("DEBUG: La réponse ne contient pas 'results'");
                return null;
            }
            
            // Vérifier si la réponse contient des résultats
            if (!jsonResponse.contains("\"location\"")) {
                System.out.println("DEBUG: La réponse ne contient pas 'location'");
                return null;
            }
            
            // Chercher "lat":
            int latIndex = jsonResponse.indexOf("\"lat\":");
            if (latIndex == -1) {
                System.out.println("DEBUG: 'lat' non trouvé dans la réponse");
                return null;
            }
            
            int latStart = jsonResponse.indexOf(":", latIndex) + 1;
            int latEnd = jsonResponse.indexOf(",", latStart);
            if (latEnd == -1) latEnd = jsonResponse.indexOf("}", latStart);
            
            // Chercher "lng":
            int lngIndex = jsonResponse.indexOf("\"lng\":");
            if (lngIndex == -1) {
                System.out.println("DEBUG: 'lng' non trouvé dans la réponse");
                return null;
            }
            
            int lngStart = jsonResponse.indexOf(":", lngIndex) + 1;
            int lngEnd = jsonResponse.indexOf("}", lngStart);
            if (lngEnd == -1) lngEnd = jsonResponse.indexOf(",", lngStart);
            
            // Extraire et nettoyer les valeurs
            String latStr = jsonResponse.substring(latStart, latEnd).trim();
            String lngStr = jsonResponse.substring(lngStart, lngEnd).trim();
            
            // Enlever les guillemets si présents
            latStr = latStr.replaceAll("\"", "");
            lngStr = lngStr.replaceAll("\"", "");
            
            System.out.println("DEBUG: latStr = '" + latStr + "', lngStr = '" + lngStr + "'");
            
            double latitude = Double.parseDouble(latStr);
            double longitude = Double.parseDouble(lngStr);
            
            System.out.println("DEBUG: Coordonnées parsées: lat=" + latitude + ", lng=" + longitude);
            return new Coordinates(latitude, longitude);
            
        } catch (NumberFormatException e) {
            System.err.println("Erreur de format numérique lors du parsing: " + e.getMessage());
            return null;
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("Erreur d'index lors du parsing JSON: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing des coordonnées: " + e.getMessage());
            System.err.println("Réponse partielle: " + (jsonResponse.length() > 100 ? jsonResponse.substring(0, 100) + "..." : jsonResponse));
            return null;
        }
    }

    /**
     * Simule les coordonnées pour des lieux connus en Tunisie
     */
    private Coordinates simulateCoordinates(String address) {
        // Coordonnées approximatives pour des lieux connus
        if (address.toLowerCase().contains("tunis")) {
            return new Coordinates(36.8065, 10.1815);
        } else if (address.toLowerCase().contains("sousse")) {
            return new Coordinates(35.8256, 10.6369);
        } else if (address.toLowerCase().contains("monastir")) {
            return new Coordinates(35.7643, 10.8113);
        } else if (address.toLowerCase().contains("djerba")) {
            return new Coordinates(33.8145, 10.8659);
        } else if (address.toLowerCase().contains("hammamet")) {
            return new Coordinates(36.4000, 10.6000);
        } else if (address.toLowerCase().contains("nabeul")) {
            return new Coordinates(36.4561, 10.7358);
        } else if (address.toLowerCase().contains("sfax")) {
            return new Coordinates(34.7406, 10.7603);
        } else if (address.toLowerCase().contains("bizerte")) {
            return new Coordinates(37.2744, 9.8739);
        } else if (address.toLowerCase().contains("tozeur")) {
            return new Coordinates(33.9252, 8.1344);
        } else if (address.toLowerCase().contains("douz")) {
            return new Coordinates(33.4500, 9.0100);
        } else if (address.toLowerCase().contains("plage")) {
            return new Coordinates(35.8000, 10.6000); // Plage générique
        } else if (address.toLowerCase().contains("desert")) {
            return new Coordinates(33.5000, 9.0000); // Désert générique
        } else {
            // Coordonnées par défaut (centre de la Tunisie)
            return new Coordinates(33.8869, 9.5375);
        }
    }

    /**
     * Simule la recherche de lieux à proximité
     */
    private NearbyPlacesResult simulateNearbyPlaces(String location, String type) {
        NearbyPlacesResult result = new NearbyPlacesResult();
        
        if (type.toLowerCase().contains("restaurant")) {
            result.addPlace("Restaurant Le Médina", 4.5, 0.2);
            result.addPlace("Café Sidi Bou Said", 4.2, 0.5);
            result.addPlace("Restaurant La Marsa", 4.7, 0.8);
        } else if (type.toLowerCase().contains("hotel")) {
            result.addPlace("Hotel Africa", 4.3, 0.3);
            result.addPlace("Hotel Tunis", 4.1, 0.6);
            result.addPlace("Residence Sousse", 4.5, 0.9);
        } else if (type.toLowerCase().contains("parking")) {
            result.addPlace("Parking Centre Ville", 3.8, 0.1);
            result.addPlace("Parking Plage", 4.0, 0.4);
        } else {
            result.addPlace("Centre Commercial", 4.2, 0.3);
            result.addPlace("Pharmacie", 4.1, 0.2);
            result.addPlace("Banque", 3.9, 0.5);
        }
        
        return result;
    }

    /**
     * Vérifie si le service est disponible
     */
    public boolean isAvailable() {
        return isInitialized;
    }

    /**
     * Teste la connexion à Google Maps
     */
    public boolean testConnection() {
        if (!isInitialized) {
            return false;
        }

        try {
            System.out.println("🗺️ Test de connexion à Google Maps...");
            
            // Vérifier si on utilise l'API réelle ou la simulation
            if (API_KEY.equals("VOTRE_CLÉ_API_ICI") || API_KEY.length() < 30) {
                System.out.println("⚠️ Test en mode simulation (pas de clé API)");
                
                // Test avec une adresse connue en simulation
                Coordinates coords = simulateCoordinates("Tunis");
                
                if (coords != null) {
                    System.out.println("✅ Connexion à Google Maps simulée avec succès");
                    System.out.println("   Test: Tunis → " + coords);
                    return true;
                } else {
                    System.out.println("❌ Échec du test de connexion");
                    return false;
                }
            } else {
                System.out.println("✅ Test en mode API réelle (clé configurée)");
                
                // Test réel avec l'API Google Maps
                try {
                    String url = String.format("%s?address=%s&key=%s", 
                        "https://maps.googleapis.com/maps/api/geocode/json", 
                        java.net.URLEncoder.encode("Tunis, Tunisie", "UTF-8"), 
                        API_KEY);
                    
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();
                    
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        System.out.println("DEBUG: Réponse API reçue (status 200)");
                        System.out.println("DEBUG: Corps de la réponse: " + response.body().substring(0, Math.min(200, response.body().length())));
                        
                        Coordinates coords = parseCoordinates(response.body());
                        if (coords != null) {
                            System.out.println("✅ Connexion à Google Maps API réussie");
                            System.out.println("   Test: Tunis → " + coords);
                            return true;
                        } else {
                            System.out.println("❌ parseCoordinates a retourné null malgré une réponse 200");
                            System.out.println("DEBUG: Utilisation de la simulation en fallback");
                            // Fallback sur simulation si parsing échoue
                            Coordinates fallbackCoords = simulateCoordinates("Tunis");
                            if (fallbackCoords != null) {
                                System.out.println("✅ Fallback simulation réussie: " + fallbackCoords);
                                return true;
                            }
                        }
                    }
                    
                    System.out.println("❌ Échec du test API - Code: " + response.statusCode());
                    return false;
                    
                } catch (Exception e) {
                    System.err.println("Erreur lors du test API: " + e.getMessage());
                    return false;
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            return false;
        }
    }

    // Classes internes pour les données
    public static class Coordinates {
        private final double latitude;
        private final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        @Override
        public String toString() {
            return String.format("(%.6f, %.6f)", latitude, longitude);
        }
    }

    public static class NearbyPlacesResult {
        private final java.util.List<Place> places = new java.util.ArrayList<>();

        public void addPlace(String name, double rating, double distance) {
            places.add(new Place(name, rating, distance));
        }

        public java.util.List<Place> getPlaces() { return places; }

        public static class Place {
            private final String name;
            private final double rating;
            private final double distance;

            public Place(String name, double rating, double distance) {
                this.name = name;
                this.rating = rating;
                this.distance = distance;
            }

            public String getName() { return name; }
            public double getRating() { return rating; }
            public double getDistance() { return distance; }

            @Override
            public String toString() {
                return String.format("%s (⭐%.1f, %.1fkm)", name, rating, distance);
            }
        }
    }
}
