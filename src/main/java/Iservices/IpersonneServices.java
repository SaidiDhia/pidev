package Iservices;

import Entites.Personne;

import java.util.List;

public interface IpersonneServices {
    void ajouterPersonne(Personne p);
    void modifierPersonne(Personne p);
    void supprimerPersonne(int id);
    List<Personne> afficherPersonnes();
}
