package com.example.pi_dev.Entities.Events;

public enum CategorieActivite {
    DESERT("Désert", "🏜️"),
    MER("Mer", "🌊"),
    AERIEN("Aérien", "🪂"),
    NATURE("Nature", "🌳"),
    CULTURE("Culture", "🏛️");
    
    private final String nom;
    private final String icone;
    
    CategorieActivite(String nom, String icone) {
        this.nom = nom;
        this.icone = icone;
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getIcone() {
        return icone;
    }
    
    @Override
    public String toString() {
        return nom;
    }
}
