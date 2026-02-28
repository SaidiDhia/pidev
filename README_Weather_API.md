# 🌤️ Intégration API OpenWeatherMap

## 📋 Description
Le système intègre maintenant l'API OpenWeatherMap pour afficher les conditions météo en temps réel et alerter les utilisateurs des risques potentiels pour les activités de plein air.

## 🔧 Configuration

### 1. Obtenir une clé API
1. Créez un compte gratuit sur [OpenWeatherMap](https://openweathermap.org/api)
2. Récupérez votre clé API gratuite
3. Remplacez `YOUR_API_KEY_HERE` dans le fichier `weather-config.properties`

### 2. Fichier de configuration
**Chemin** : `src/main/resources/weather-config.properties`

```properties
weather.api.key=79f2e054a0d8ea223d4c50292e3c1f1c
weather.default.city=Tunis
weather.units=metric
weather.language=fr
```

## 🚨 Alertes de sécurité

Le système détecte automatiquement les conditions météo dangereuses et affiche des alertes pour :

### ☔ **Pluie**
- **MER** : Plongée sous-marine, Bateau
- **AERIEN** : Parapente, ULM
- **NATURE** : Randonnée en montagne

### ❄️ **Neige**
- **MER** : Bateau, Kayak
- **AERIEN** : Vol en montgolfière, Parachutisme
- **NATURE** : Camping, Randonnée

### ⛈️ **Orage**
- **MER** : Toutes activités nautiques
- **AERIEN** : Toutes activités aériennes
- **NATURE** : Randonnée, Escalade
- **DESERT** : Safari, 4x4

### 🌡️ **Chaleur extrême (>40°C)**
- **DESERT** : Trekking, Safari
- **NATURE** : Randonnée longue
- **MER** : Plongée prolongée

## 🎯 Fonctionnalités

### Widget météo dans ActiviteCard
- Affichage en temps réel : température, vent, humidité
- Alertes visuelles selon les conditions
- Messages de sécurité personnalisés

### Alertes automatiques
- Détection automatique des risques
- Messages d'avertissement contextuels
- Recommandations d'activités alternatives

## 📱 Interface utilisateur

### Composants météo
- 🌤️ Icônes météo intuitives
- 📊 Données claires et lisibles
- ⚠️ Alertes colorées (jaune/orange/rouge)
- ✅ Messages de confirmation pour conditions favorables

### Messages d'alerte
```
⚠️ Conditions météo défavorables:
• Plongée sous-marine (MER)
• Parapente (AERIEN)
• Randonnée en montagne (NATURE)

Météo actuelle: Pluie modérée
Température: 18°C
```

## 🔧 Installation

### Dépendances requises
```xml
<!-- Ajouter au pom.xml -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

### Fichiers modifiés
- `Services/WeatherService.java` - Service météo principal
- `Controllers/ActiviteCardController.java` - Intégration du widget
- `views/ActiviteCard.fxml` - Ajout du conteneur météo
- `resources/weather-config.properties` - Configuration API

## 🌍 Personnalisation

### Changer la ville par défaut
Modifiez `weather.default.city` dans le fichier de configuration.

### Ajouter de nouvelles alertes
Étendez la map `RISK_ACTIVITIES` dans `WeatherService.java`.

### Personnaliser les seuils
Modifiez les conditions dans `determineWeatherCondition()`.

## 🚀 Utilisation

1. Configurez votre clé API
2. Lancez l'application
3. Ouvrez une carte d'activité
4. Le widget météo s'affiche automatiquement
5. Les alertes s'affichent si nécessaire

## 📞 Support

Pour toute question sur l'intégration météo :
- Documentation OpenWeatherMap : https://openweathermap.org/api
- Issues GitHub : Créer un ticket avec le tag "weather-api"

---

**Note** : L'API OpenWeatherMap offre 1000 appels/jour gratuits, suffisant pour un usage personnel ou de développement.
