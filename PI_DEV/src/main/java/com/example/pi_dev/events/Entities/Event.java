package com.example.pi_dev.events.Entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Event {

    private int id;
    private int idActivite; // FK vers Activite
    private String lieu;
    private String adresseComplete;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private BigDecimal prix;
    private Integer capaciteMax;
    private Integer placesDisponibles;
    private String organisateur;
    private String materielsNecessaires;
    private String image;
    private StatutEvent statut;
    private Timestamp dateCreation;
    private Timestamp dateModification;

    // Pour affichage (non stocké en BD)
    private Activite activite;

    public enum StatutEvent {
        A_VENIR,
        EN_COURS,
        ANNULE,
        TERMINE
    }

    public Event() {}

    public Event(int idActivite, String lieu, LocalDateTime dateDebut,
                 LocalDateTime dateFin, double capaciteMax, int prix) {

        this.idActivite = idActivite;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capaciteMax = (int) capaciteMax;
        this.placesDisponibles = (int) capaciteMax;
        this.prix = BigDecimal.valueOf(prix);
        this.statut = StatutEvent.A_VENIR;
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdActivite() { return idActivite; }
    public void setIdActivite(int idActivite) { this.idActivite = idActivite; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public Integer getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(Integer placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public String getOrganisateur() { return organisateur; }
    public void setOrganisateur(String organisateur) { this.organisateur = organisateur; }

    public String getMaterielsNecessaires() { return materielsNecessaires; }
    public void setMaterielsNecessaires(String materielsNecessaires) { this.materielsNecessaires = materielsNecessaires; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public StatutEvent getStatut() { return statut; }
    public void setStatut(StatutEvent statut) { this.statut = statut; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    public Activite getActivite() { return activite; }
    public void setActivite(Activite activite) { this.activite = activite; }
}
