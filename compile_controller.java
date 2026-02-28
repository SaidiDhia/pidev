import java.io.*;
import java.nio.file.*;

public class compile_controller {
    public static void main(String[] args) {
        try {
            // Copier le controller source vers un fichier temporaire
            Path source = Paths.get("src/main/java/Controllers/modifierEventController.java");
            Path temp = Paths.get("modifierEventController_temp.java");
            Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Controller copié avec succès");
            System.out.println("Méthode goToCatalogue présente: " + Files.readAllLines(temp).stream().anyMatch(line -> line.contains("goToCatalogue")));
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}
