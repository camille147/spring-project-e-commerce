# springecommerceapi — Documentation rapide


Ce que contient ce README
- [x] Présentation du module et responsabilités
- [x] Liste des routes/endpoints (méthodes, chemins, paramètres, corps, réponses)
- [x] Swagger / OpenAPI (où y accéder)
- [x] Authentification (obtention et usage du JWT)
- [x] Exemples curl / JSON pour les endpoints principaux
- [x] Instructions pour lancer localement et variables d'environnement utiles
- [x] Notes sur import.sql et dépannage rapide

---

## 1. Présentation
Le module `springecommerceapi` expose l'API REST du projet Spring E-Commerce. Il fournit les endpoints pour gérer les utilisateurs, produits, catégories, commandes, adresses, images et promotions. Les endpoints sont sous `/api` et certaines routes d'administration sont protégées (nécessitent un rôle ADMIN).

Base URL (par défaut local): http://localhost:8080
Préfixe API: `/api` (ex. `http://localhost:8080/api/products`)

---

## 2. Authentification
- Endpoints d'auth :
  - POST `/api/auth/login` — corps: `{ "email": "...", "password": "..." }` -> réponse: `JwtResponse` { token, type, id, email, roles }
  - POST `/api/auth/signin` — inscription: `SignupRequest` (email, firstName, lastName, password, birthDate)
  - GET `/api/auth/test` — endpoint de test non protégé

- JWT :
  - La projection utilise `io.jsonwebtoken` et un `JwtUtil` qui lit `jwt.secret` et `jwt.expiration` depuis `application.properties` (ou variables d'environnement). Exemple de propriétés :
    - `JWT_SECRET` (ou `jwt.secret`) — clé secrète (fortement recommandé de définir une clé >= 256 bits)
    - `JWT_EXPIRATION` (ou `jwt.expiration`) — durée en ms (ex. 86400000 = 24h)
  - Pour appeler un endpoint protégé, ajouter l'entête HTTP :

```http
Authorization: Bearer <token>
```

- Le filtre `AuthTokenFilter` extrait le token du header `Authorization` et authentifie la requête.

---

## 3. Swagger / OpenAPI
Le projet inclut `springdoc-openapi-starter-webmvc-ui` et une config `OpenApiConfig`.
- OpenAPI JSON : `GET /v3/api-docs`
- Swagger UI (interface web) :
  - `http://localhost:8080/swagger-ui.html`
  - ou `http://localhost:8080/swagger-ui/index.html`

Si Swagger n'apparaît pas, vérifier que l'application est démarrée et que la dépendance `springdoc-openapi-starter-webmvc-ui` est bien présente dans `pom.xml` (elle l'est).

---

## 4. Liste des endpoints (résumé)

Le(s) tableau(x) ci-dessous récapitule(nt) les principaux endpoints exposés par l'API (préfixe `/api`).

### Auth

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| POST | `/api/auth/login` | Public | `LoginRequest` (`{email, password}`) | Authentification - retourne `JwtResponse` |
| POST | `/api/auth/signin` | Public | `SignupRequest` / `UserDto` | Inscription utilisateur |
| GET | `/api/auth/test` | Public | - | Endpoint de test |

### Users

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/admin/users` | ADMIN | - | Liste des utilisateurs |
| GET | `/api/admin/get={id}` | ADMIN | - | Récupérer un utilisateur par id |
| POST | `/api/admin/users` | ADMIN | `UserDto` | Créer un utilisateur |
| PUT | `/api/admin/{id}` | ADMIN | `UserDto` | Mettre à jour un utilisateur |
| GET | `/api/user/profile` | Authenticated | - | Profil de l'utilisateur connecté |
| PUT | `/api/user/profile` | Authenticated | `UserDto` | Mettre à jour son profil |
| PATCH | `/api/user/password/id={id}` | Authenticated | `String` (nouveau mot de passe) | Changer le mot de passe (par id) |

### Produits

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/products` | Public | - | Lister tous les produits |
| GET | `/api/products/search/{name}` | Public | - | Rechercher des produits par nom |
| GET | `/api/products/{id}` | Public | - | Récupérer un produit par id (numérique) |
| POST | `/api/admin/products` | ADMIN | `ProductDto` | Créer un produit |
| PUT | `/api/admin/products/{id}` | ADMIN | `ProductDto` | Mettre à jour un produit |
| DELETE | `/api/admin/products/{id}` | ADMIN | - | Désactiver/supprimer un produit (met isEnabled=false) |
| PATCH | `/api/admin/products/{id}/stock` | ADMIN | `{ "quantity": n }` ou `{ "delta": +/-n }` | Mettre à jour le stock (set ou delta) |

### Catégories

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/category` | Public | - | Lister les catégories |
| GET | `/api/category/{id}` | Public | - | Récupérer une catégorie par id |
| POST | `/api/admin/category` | ADMIN | `CategoryDto` | Créer une catégorie |
| PUT | `/api/admin/category/{id}` | ADMIN | `CategoryDto` | Mettre à jour une catégorie |
| DELETE | `/api/admin/category/{id}` | ADMIN | - | Supprimer une catégorie |

### Commandes (Orders)

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/admin/orders` | ADMIN | - | Lister toutes les commandes |
| GET | `/api/orders/{id}` | Owner or ADMIN | - | Récupérer une commande (propriétaire ou ADMIN) |
| POST | `/api/orders` | Authenticated | `OrderDto` | Créer une commande (utilisateur connecté; ADMIN peut préciser `userId`) |
| PATCH | `/api/admin/orders/{id}/status` | ADMIN | `{ "status": n }` | Mettre à jour le statut d'une commande |

### Order lines

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/order-lines` | Public/Protégé | - | Lister les order lines |
| GET | `/api/order-lines/{id}` | Public/Protégé | - | Récupérer un order line |
| POST | `/api/order-lines` | Public/Protégé | `OrderLineDto` | Créer un order line |
| PUT | `/api/order-lines/{id}` | Public/Protégé | `OrderLineDto` | Mettre à jour un order line |
| DELETE | `/api/order-lines/{id}` | Public/Protégé | - | Supprimer un order line |

### Adresses

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/addresses/{id}` | Owner or ADMIN | - | Récupérer une adresse (seul le propriétaire ou ADMIN) |
| POST | `/api/addresses` | Authenticated | `AddressDto` | Créer une adresse (utilisateur authentifié) |
| PUT | `/api/addresses/{id}` | Owner or ADMIN | `AddressDto` | Mettre à jour une adresse |
| DELETE | `/api/addresses/{id}` | Owner or ADMIN | - | Supprimer une adresse |

### Images / Pictures

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/admin/pictures/{id}` | ADMIN | - | Récupérer une picture (admin) |
| GET | `/api/admin/products/{productId}/pictures` | ADMIN | - | Lister la galerie d'un produit (admin) |
| POST | `/api/admin/products/{productId}/pictures` | ADMIN | `PictureDto` | Ajouter une image à un produit |
| PUT | `/api/admin/pictures/{id}` | ADMIN | `PictureDto` | Mettre à jour une image |
| DELETE | `/api/admin/pictures/{id}` | ADMIN | - | Supprimer une image |

### Product pictures (ressource séparée)

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/product-pictures` | Public/Protégé | - | Lister product-pictures |
| GET | `/api/product-pictures/{id}` | Public/Protégé | - | Récupérer product-picture par id |
| POST | `/api/product-pictures` | Public/Protégé | `ProductPictureDto` | Créer |
| PUT | `/api/product-pictures/{id}` | Public/Protégé | `ProductPictureDto` | Mettre à jour |
| DELETE | `/api/product-pictures/{id}` | Public/Protégé | - | Supprimer |

### Product promotions

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/product-promotions` | Public/Protégé | - | Lister product-promotions |
| GET | `/api/product-promotions/{id}` | Public/Protégé | - | Récupérer product-promotion par id |
| POST | `/api/product-promotions` | Public/Protégé | `ProductPromotionDto` | Créer |
| PUT | `/api/product-promotions/{id}` | Public/Protégé | `ProductPromotionDto` | Mettre à jour |
| DELETE | `/api/product-promotions/{id}` | Public/Protégé | - | Supprimer |

### Promotions

| Méthode | Chemin | Auth | Corps (DTO / payload) | Description |
|---|---|---:|---|---|
| GET | `/api/promotions` | Public | - | Lister les promotions |
| GET | `/api/promotions/{id}` | Public | - | Récupérer une promotion |
| POST | `/api/admin/promotions` | ADMIN | `PromotionDto` | Créer une promotion |

---

## 5. Schémas (DTO) — exemples utiles
Exemples basés sur les DTOs présents dans `springecommerceapi`.

- ProductDto (exemple POST/PUT):

```json
{
  "productName": "Chaussures sport",
  "description": "Chaussures confortables",
  "price": 59.99,
  "quantity": 100,
  "categoryIds": [1, 2],
  "isEnabled": true,
  "color": "Noir",
  "brand": "MarqueX",
  "reference": "MX-001"
}
```

- UserDto / Signup (pour `signin`) :

```json
{
  "firstName": "Alice",
  "lastName": "Dupont",
  "email": "alice@example.com",
  "birthDate": "1990-05-15",
  "password": "motdepasse123"
}
```

- AddressDto (créer adresse) :

```json
{
  "street": "12 rue Exemple",
  "city": "Paris",
  "zipCode": "75001",
  "country": "FR",
  "isActive": true
}
```

- OrderDto (création, minimal) :

```json
{
  "orderNumber": "CMD-20260129-001",
  "total": 159.98,
  "status": 0,
  "addressId": 5,
  "orderLineIds": [10, 11]
}
```

- PATCH stock payloads :
  - Set absolute quantity: `{ "quantity": 42 }`
  - Delta: `{ "delta": -1 }`

- Update order status (ADMIN): `{ "status": 1 }`

---

## 6. Exemples curl
1) Login (obtenir JWT)

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"motdepasse123"}'
```
Réponse attendue (extrait):
```json
{ "token": "ey...", "type": "Bearer", "id": 1, "email": "alice@example.com", "roles": [] }
```

2) Utiliser le token pour lister les produits (GET public) :

```bash
curl -s http://localhost:8080/api/products
```

3) Créer un produit (ADMIN) :

```bash
curl -s -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <VOTRE_TOKEN_ADMIN>" \
  -d '{"productName":"Nouveau produit","price":9.9,"quantity":10}'
```

4) Créer une adresse (utilisateur connecté) :

```bash
curl -s -X POST http://localhost:8080/api/addresses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <VOTRE_TOKEN>" \
  -d '{"street":"12 rue Exemple","city":"Paris","zipCode":"75001","country":"FR"}'
```

5) Créer une commande (utilisateur connecté) :

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <VOTRE_TOKEN>" \
  -d '{"orderNumber":"CMD-001","total":59.99,"addressId":5,"orderLineIds":[1]}'
```

---

## 7. Lancer l'API localement
- Avec Maven (dev) :

```bash
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

- Générer le jar et l'exécuter :

```bash
./mvnw package -DskipTests
java -jar target/spring-e-commerce-0.0.1-SNAPSHOT.jar
```
- Avec Docker :
```bash
docker-compose up --build
##Puis pour le relancer :
docker-compose down -v
rm -rf data/*.db
./mvnw clean package -DskipTests
docker-compose up --build
```


Variables d'environnement / propriétés utiles (repris depuis `application.properties`):
- SERVER_PORT (ex. 8080)
- DB_USER / DB_PASSWORD
- JWT_SECRET (clé JWT) — par défaut `dev-secret` dans `application.properties` (utiliser une vraie valeur en prod)
- JWT_EXPIRATION (ms)

---

## 8. Import de données
Le fichier `src/main/java/org/example/shared/configuration/DataInitializer.java` est exécuté au démarrage. Attention : en production, évitez d'exécuter des scripts d'initialisation non contrôlés.

C'est un ficher de configuration de Faker qui insère des données de test dans la base (utilisateurs, produits, catégories, commandes, adresses, images, promotions)

---

## 9. FAQ / Dépannage rapide
- Swagger UI introuvable : vérifiez que l'application est démarrée et essayez `/swagger-ui/index.html` ou `/v3/api-docs`.
- Erreur 401 / token invalide : assurez-vous d'envoyer `Authorization: Bearer <token>` et que la valeur du token n'est pas corrompue.
- Warning `jwt.secret is too short` : définissez `JWT_SECRET` avec une clé suffisante (>= 256 bits recommended).
- Accès admin refusé : vérifier les rôles retournés dans `roles` du `JwtResponse` et que l'utilisateur a `ROLE_ADMIN`.

---

## 10. Où regarder le code source
- Contrôleurs API : `src/main/java/org/example/springecommerceapi/controller/` (liste complète des controllers)
- DTOs : `src/main/java/org/example/springecommerceapi/model/dto/`
- Sécurité / JWT : `src/main/java/org/example/springecommerceapi/security/`
- OpenAPI config : `src/main/java/org/example/springecommerceapi/configuration/OpenApiConfig.java`

---