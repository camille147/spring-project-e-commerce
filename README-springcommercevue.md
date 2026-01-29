## Frontend & Interface Utilisateur

L'interface utilisateur de l'application a été conçue pour être à la fois responsive et rapide, en utilisant le moteur de template natif de Spring Boot.

### Technologies utilisées

* **HTML5 / CSS3** : Structure et style de base.
* **Thymeleaf** : Moteur de template Java pour le rendu dynamique des vues côté serveur.
* **Tailwind CSS** : Framework CSS utilitaire pour le design rapide et cohérent des composants modernes (boutons, cartes produits, navigation, etc.).
* **Alpine.js** : Pour l'interactivité légère côté client sans la lourdeur d'un framework JS complet.

### Choix de conception

Une approche hybride a été adoptée pour le développement des interfaces :

1.  **Pages d'Authentification (Login / Register)** :
    * Ces pages ont été réalisées en **CSS pur** (sans framework) afin de garder une maîtrise totale sur le design personnalisé et d'optimiser le chargement critique de ces points d'entrée.

2.  **Reste de l'application (Shop, Panier, Profil, etc.)** :
    * Face aux contraintes de temps et pour assurer une maintenabilité maximale, nous avons basculé vers **Tailwind CSS**.
    * L'utilisation de composants Tailwind pré-construits a permis d'accélérer considérablement le développement tout en garantissant une charte graphique uniforme et un design "Mobile First".

### Architecture des Templates & Séparation des Responsabilités

Pour garantir une meilleure maintenabilité et une séparation claire des contextes, l'arborescence des vues Thymeleaf (`src/main/resources/templates`) a été structurée par rôle :

* **`/user` (Front-Office)** : Contient toutes les vues destinées au client final (Catalogue, Panier, Tunnel d'achat, Profil). Cela permet d'isoler la logique et le design propres à l'expérience client.
* **`/admin` (Back-Office)** : Regroupe exclusivement les pages de gestion (Dashboard, CRUD Produits, Commandes). Cette séparation facilite la gestion de la sécurité (routes protégées par rôle `ADMIN`).
* **Modularité (Fragments)** : Nous avons exploité le système de fragments de Thymeleaf (`th:replace`). La barre de navigation et le pied de page sont définis dans des fichiers uniques (`navbar.html`, `footer.html`) et réinjectés dynamiquement sur toutes les pages. Cela évite la duplication de code et simplifie les mises à jour globales du design.

### Conformité RGPD & Pages Légales

Dans une démarche de professionnalisme et de respect des normes actuelles, une section dédiée (`/rgpd`) a été intégrée. Elle regroupe les pages statiques indispensables à tout site e-commerce :
* **Mentions Légales** (`legal.html`)
* **Politique de Confidentialité** (`privacy-policy.html`)
* **Conditions Générales d'Utilisation** (`terms.html`)

Ces pages sont accessibles depuis le pied de page (*footer*) global, assurant une conformité visuelle et structurelle avec les standards du web.

### Gestion des Erreurs Personnalisée

Afin de ne pas briser l'expérience utilisateur (UX) en cas de problème, nous avons remplacé la "Whitelabel Error Page" par défaut de Spring Boot par des pages personnalisées, situées dans le dossier standard `/error` :
* **`404.html`** : Page "Introuvable", permettant à l'utilisateur de retourner facilement à la boutique.
* **`500.html`** : Page "Erreur Serveur", affichant un message rassurant en cas de problème technique interne.

Ces pages conservent la charte graphique du site (Navbar, Footer, Typographie), garantissant une navigation fluide même lors des imprévus.