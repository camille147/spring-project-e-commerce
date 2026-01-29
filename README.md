# Spring E-Commerce

Ce dépôt contient le projet Spring E-Commerce.

Pour la documentation de l'API REST exposée par le module `springecommerceapi`, voir :

- `README-springecommerceapi.md` (documentation détaillée des routes, Swagger, JWT et exemples d'utilisation)
- `README-Prerequis.md` (documentation détaillée des prérequis d'installation de Docker, Docker Compose et Java 17)
- `README-springecommercevue.md` (documentation détaillée des controllers et des vues)

## Description et Objectifs
**TechZone** est une application de e-commerce spécialisée dans les produits High-Tech. Le projet vise à fournir une expérience d'achat
tout en offrant une interface d'administration complète et une conformité stricte aux réglementations **RGPD**.

### Objectifs clés :
* **Expérience Utilisateur :** Catalogue fluide, tunnel d'achat optimisé et gestion de profil.
* **Gestion Admin :** Contrôle total sur les stocks, les promotions complexes et le cycle de vie des commandes.
* **Sécurité & Éthique :** Implémentation du droit à l'oubli par anonymisation et protection des routes par rôles.
* **Interopérabilité :** Exposition d'une API REST sécurisée par JWT.


## Stack Technique

### Backend
* **Java 17** & **Spring Boot 3.4**
* **Spring Security** : Authentification hybride (Session pour le Web, JWT pour l'API).
* **Spring Data JPA** : Persistance avec Hibernate.
* **H2 Database** : Base de données relationnelle 
* **Java Faker** : Génération de jeux de données massifs et cohérents.

### Frontend
* **Thymeleaf** : Moteur de template pour le rendu côté serveur.
* **Tailwind CSS** : Framework utilitaire pour un design UI/UX moderne.
* **Alpine.js** : Interactivité légère (modales, notifications toast, transitions).
* **CSS Pur** : Utilisé spécifiquement pour les pages critiques (Login/Register).


## Architecture du Projet
Le projet utilise une architecture modulaire et découpée en couches pour maximiser la maintenabilité :


* **`org.example.shared`** : Noyau commun contenant les entités JPA, les repositories et les services métier.
* **`org.example.springecommerce`** : Module Web gérant les contrôleurs Thymeleaf et les vues.
* **`org.example.springecommerceapi`** : Module API gérant les endpoints REST et la sécurité JWT.
* **`org.example.shared.configuration`** : Initialisation automatique de la base de données via `DataInitializer`.

## Instructions de Lancement

### Lancement avec Docker (Recommandé)
1. Assurez-vous que Docker Desktop est actif.
2. À la racine du projet, exécutez :
   ```bash
   docker-compose up --build

### URLs de développement


    Swagger UI (API) : http://localhost:8080/swagger-ui/index.html

    Console H2 : http://localhost:8080/h2-console

        JDBC URL : jdbc:h2:file:/app/data/techzonedb

        User : SA | Password : (vide)


## Comptes de test


* ADMINISTRATEUR : admin@admin.com -> adminadmin
* ADMINISTRATEUR : admin@test.com -> jesuisadmin123!


* UTILISATEUR : test@test.com -> password
* UTILISATEUR : test2@test.com -> password2
* UTILISATEUR : test3@test.com -> password3

## Utilisation de l'Intelligence Artificielle
L'IA a été utilisée comme partenaire de développement pour :

* UI/UX : Génération de structures de composants Tailwind et optimisation de la responsivité.
* Débogage : Résolution des erreurs 
* Données : Conseil sur la création de scénarios Faker complexes 
* Outils : ChatGPT et Gemini