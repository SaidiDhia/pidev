-- Script pour ajouter la colonne catégorie à la table activites
-- Exécuter ce script sur votre base de données MySQL

-- Ajouter la colonne catégorie à la table activites
ALTER TABLE activites 
ADD COLUMN categorie ENUM('DESERT', 'MER', 'AERIEN', 'NATURE', 'CULTURE') NOT NULL DEFAULT 'NATURE';

-- Mettre à jour les activités existantes avec une catégorie par défaut
UPDATE activites 
SET categorie = 'NATURE' 
WHERE categorie IS NULL OR categorie = '';

-- Afficher les activités pour vérification
SELECT id, titre, type_activite, categorie, date_creation 
FROM activites 
ORDER BY date_creation DESC;
