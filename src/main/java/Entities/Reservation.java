package Entities;

import java.sql.Timestamp;

public class Reservation {

    private int id;
    private int idEvent; // FK vers Event
    private String nomComplet;
    private String email;
    private String telephone;
    private Integer nombrePersonnes;
    private StatutReservation statut;
    private Timestamp dateCreation;
    private Timestamp dateModification;

    // Pour affichage
    private Event event;

    public enum StatutReservation {
        EN_ATTENTE,
        CONFIRMEE,
        ANNULEE,
        TERMINEE
    }

    public Reservation() {
        this.nombrePersonnes = 1;
        this.statut = StatutReservation.EN_ATTENTE;
    }

    public Reservation(int idEvent, String nomComplet, String email,
                       String telephone, Integer nombrePersonnes) {
        this();
        this.idEvent = idEvent;
        this.nomComplet = nomComplet;
        this.email = email;
        this.telephone = telephone;
        this.nombrePersonnes = nombrePersonnes;
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEvent() { return idEvent; }
    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Integer getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(Integer nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}
