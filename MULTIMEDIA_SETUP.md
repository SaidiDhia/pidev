# 🎉 Guide d'Installation des Fonctionnalités Multimédia

## 📋 Vue d'ensemble

Ce guide vous aide à configurer et tester les nouvelles fonctionnalités d'images multiples et vidéos YouTube pour votre système d'événements.

## 🚀 Étapes d'installation

### 1️⃣ Configuration de la base de données

Exécutez le script SQL fourni :

```bash
# Avec MySQL Workbench ou phpMyAdmin
mysql -u root -p wonderlust_db < database_setup.sql
```

Ou exécutez manuellement :

```sql
-- Créer la table des photos
CREATE TABLE event_photos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_event INT NOT NULL,
    chemin_photo VARCHAR(500) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_event) REFERENCES events(id) ON DELETE CASCADE
);

-- Ajouter les colonnes manquantes
ALTER TABLE events 
ADD COLUMN video_youtube VARCHAR(500),
ADD COLUMN organisateur VARCHAR(255),
ADD COLUMN materiels_necessaires TEXT,
ADD COLUMN image VARCHAR(500),
ADD COLUMN date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
```

### 2️⃣ Vérification de l'interface

L'interface `Event.fxml` a été modifiée pour inclure :

- ✅ **ImageView** pour l'image principale
- ✅ **Bouton** pour ajouter des photos multiples
- ✅ **ScrollPane** pour afficher les vignettes
- ✅ **Champ** pour l'URL YouTube
- ✅ **Labels** d'aide et de statut

### 3️⃣ Test des fonctionnalités

#### 🧪 Test rapide avec la classe de test :

```bash
# Compiler et exécuter le test
cd src/main/java/Controllers
javac TestEventFeatures.java
java TestEventFeatures
```

#### 🧪 Test dans l'application :

1. **Démarrer l'application**
2. **Aller à la création d'événement**
3. **Tester chaque fonctionnalité**

## 📸 Fonctionnalités d'images

### Image principale
- **Cliquez sur "📷 Image principale"**
- **Sélectionnez une image** (jpg, png, jpeg, gif, bmp)
- **Vérifiez l'aperçu** dans l'ImageView
- **Le chemin est sauvegardé** dans `uploads/events/`

### Photos multiples
- **Cliquez sur "➕ Ajouter"**
- **Sélectionnez plusieurs images** (Ctrl+Clic)
- **Les vignettes apparaissent** dans le ScrollPane
- **Supprimez avec les boutons ❌**
- **Toutes sont sauvegardées** en base

### 🎥 Fonctionnalité vidéo YouTube

#### URLs supportées :
```
✅ https://www.youtube.com/watch?v=VIDEO_ID
✅ https://youtu.be/VIDEO_ID  
✅ https://www.youtube.com/embed/VIDEO_ID
❌ https://facebook.com/video
❌ https://vimeo.com/VIDEO_ID
```

#### Validation automatique :
- **Format vérifié** à la saisie
- **Message d'erreur** si invalide
- **URL stockée** en base de données

## 🗂️ Structure des fichiers

```
votre_projet/
├── uploads/events/          # Répertoire créé automatiquement
│   ├── 1234567890_image.jpg    # Image principale
│   ├── 1234567891_photo1.jpg   # Photo 1
│   └── 1234567892_photo2.jpg   # Photo 2
├── src/main/resources/views/
│   └── Event.fxml          # Interface modifiée
├── src/main/java/Controllers/
│   ├── EventController.java  # Controller modifié
│   └── TestEventFeatures.java # Classe de test
└── database_setup.sql        # Script SQL
```

## 🧪 Tests de validation

### Test 1: Répertoire d'uploads
```bash
# Vérifier que le répertoire existe
ls -la uploads/events/

# Permissions
chmod 755 uploads/events/
```

### Test 2: Base de données
```sql
-- Vérifier les tables
SHOW TABLES;
DESCRIBE events;
DESCRIBE event_photos;
```

### Test 3: Interface
- ✅ L'interface s'ouvre sans erreur
- ✅ Les nouveaux champs sont visibles
- ✅ Les boutons répondent au clic

## 🔧 Dépannage

### Problèmes courants

#### ❌ "Aucune image sélectionnée"
**Cause**: L'ImageView n'est pas connecté au controller
**Solution**: Vérifiez `fx:id="imagePrincipaleView"` dans le FXML

#### ❌ "Erreur lors de la copie de l'image"
**Cause**: Permissions du répertoire
**Solution**: 
```bash
chmod 755 uploads/events/
# Ou créer manuellement le répertoire
```

#### ❌ "L'URL YouTube n'est pas valide"
**Cause**: Format incorrect
**Solution**: Utilisez un des formats supportés

#### ❌ "Table event_photos n'existe pas"
**Cause**: Script SQL non exécuté
**Solution**: Exécutez `database_setup.sql`

### Messages de debug utiles

Dans la console, cherchez ces messages :
```
Image principale sélectionnée: uploads/events/1234567890_image.jpg
Photo supplémentaire ajoutée: uploads/events/1234567891_photo1.jpg
Répertoire uploads créé
YouTube valide: true
```

## 🎯 Cas d'usage

### Scénario 1: Événement simple
1. **Image principale** : Logo de l'événement
2. **Photos multiples** : Photos du lieu
3. **Vidéo** : Présentation YouTube
4. **Résultat** : Page événement riche en médias

### Scénario 2: Événement premium
1. **Image principale** : Photo professionnelle
2. **Photos multiples** : Galerie complète
3. **Vidéo** : Vidéo promotionnelle
4. **Résultat** : Présentation premium

## 📊 Performance

### Optimisations intégrées
- **Noms uniques** avec timestamp
- **Stockage local** des images
- **Compression non destructive**
- **Vignettes légères** pour l'interface

### Limites recommandées
- **Images**: < 5MB par fichier
- **Photos**: Maximum 10 par événement
- **Vidéo**: 1 par événement (URL YouTube)

## 🎉 Résultat attendu

Une fois configuré, votre système permettra :

- ✅ **Sélection d'image principale** avec aperçu
- ✅ **Ajout de photos multiples** avec vignettes
- ✅ **Intégration vidéo YouTube** avec validation
- ✅ **Suppression individuelle** des photos
- ✅ **Sauvegarde complète** en base de données
- ✅ **Interface intuitive** et moderne

## 🆘 Support

Si vous rencontrez des problèmes :

1. **Vérifiez la console** pour les erreurs
2. **Exécutez la classe de test** pour valider
3. **Vérifiez les permissions** des répertoires
4. **Confirmez la structure** de la base

---

**🚀 Votre système est maintenant prêt pour les fonctionnalités multimédia !**
