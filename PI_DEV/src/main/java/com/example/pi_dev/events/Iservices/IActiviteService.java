package com.example.pi_dev.events.Iservices;

import com.example.pi_dev.events.Entities.Activite;
import java.sql.SQLException;
import java.util.List;

public interface IActiviteService {

    // CREATE
    void ajouter(Activite activite) throws SQLException;

    // READ
    List<Activite> afficher() throws SQLException;
    Activite getById(int id) throws SQLException;

    // UPDATE
    void modifier(Activite activite) throws SQLException;

    // DELETE
    void supprimer(int id) throws SQLException;
}