# Spring Boot Marketplace

Une application backend complète de marketplace généralisée développée avec Spring Boot, permettant aux utilisateurs d'acheter et de vendre des produits via des boutiques.

## 📋 Caractéristiques

### Fonctionnalités utilisateur
- Inscription et gestion des comptes utilisateurs (acheteurs, vendeurs, administrateurs)
- Gestion des adresses multiples
- Système de liste de souhaits et panier d'achat
- Historique de commandes et suivi

### Plateforme de vente
- Création et gestion de boutiques
- Gestion de produits avec catégorisation
- Système d'avis et évaluations
- Tableau de bord vendeur avec statistiques

### Processus d'achat
- Panier d'achat persistant
- Processus de commande sécurisé
- Intégration avec Stripe et PayPal pour les paiements
- Gestion complète du cycle de vie des commandes

### Fonctionnalités avancées
- Recherche rapide et pertinente grâce à Elasticsearch
- Mise en cache avec Redis pour des performances optimales
- Authentification sécurisée avec JWT
- Documentation API complète avec Swagger/OpenAPI

## 🛠️ Technologies

### Backend
- **Framework** : Spring Boot 3.x
- **Persistence** : Spring Data JPA
- **Base de données** : PostgreSQL
- **Sécurité** : Spring Security, JWT
- **Cache** : Redis
- **Recherche** : Elasticsearch
- **Documentation** : Swagger/OpenAPI

### Paiements
- Stripe API
- PayPal API

### Tests
- JUnit 5
- Mockito
- Testcontainers

## 📥 Installation

### Prérequis
- Java 17 ou supérieur
- Maven 3.8 ou supérieur
- Docker et Docker Compose (pour l'environnement de développement)

### Configuration
1. Cloner le dépôt :
```bash
git clone https://github.com/votre-username/marketplace-backend.git
cd marketplace-backend
```

2. Lancer les services nécessaires avec Docker Compose :
```bash
docker-compose up -d
```

3. Construire et exécuter l'application :
```bash
mvn clean install
mvn spring-boot:run
```

4. L'application sera accessible à l'adresse suivante :
```
http://localhost:8080/api
```

5. La documentation Swagger est disponible à :
```
http://localhost:8080/api/swagger-ui.html
```

## 🧱 Architecture

L'application suit une architecture en couches :

- **Controller** : Endpoints REST API
- **Service** : Logique métier
- **Repository** : Accès aux données
- **Model** : Entités et objets de transfert de données (DTOs)

### Structure des packages
```
com.example.marketplace/
├── config/         # Configuration Spring et sécurité
├── controller/     # Contrôleurs REST
├── model/          # Entités JPA et DTOs
├── repository/     # Repositories Spring Data
├── service/        # Services métier
├── security/       # Configuration de sécurité JWT
├── exception/      # Gestion des exceptions
└── util/           # Classes utilitaires
```

## 🔑 Sécurité

Le système d'authentification utilise JWT (JSON Web Token) :

- Les tokens ont une durée de validité de 24 heures
- Tous les endpoints protégés nécessitent un token valide
- Les différents rôles (BUYER, SELLER, ADMIN) ont des autorisations différentes

## 📊 Database Schema

Le schéma de base de données comprend les entités principales suivantes :
- Users
- Stores
- Products
- Categories
- Orders
- Payments
- Carts
- Wishlists
- Reviews

## 🧪 Tests

Exécuter les tests unitaires et d'intégration :
```bash
mvn test
```

## 🔍 Monitoring

L'application expose des endpoints Actuator pour le monitoring :
```
http://localhost:8080/api/actuator/health
http://localhost:8080/api/actuator/metrics
```



## 📬 Contact

Pour toute question ou suggestion, veuillez ouvrir une issue sur le dépôt GitHub ou contacter l'auteur à l'adresse felixndao3@gmail.com.