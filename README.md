
# 🚗 AUXIDRIVE
### Application Web de Gestion d'Assurance Automobile

![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-Frontend-7952B3?logo=bootstrap&logoColor=white)
![Status](https://img.shields.io/badge/Statut-En_développement-orange)
![Version](https://img.shields.io/badge/Version-Non_finalisée-yellow)

---

## 📖 Présentation du projet

**AUXIDRIVE** est une application web de gestion d'assurance automobile, conçue et développée dans le cadre d'un **Projet de Fin d'Études (PFE) en Licence 3 Génie Logiciel**.

Ce projet a été réalisé dans le contexte de l'entreprise **AUXIMAD**, plus précisément pour son service d'assurance automobile **AUXIASSUR**.

L'objectif principal d'AUXIDRIVE est de **digitaliser, centraliser et simplifier les processus de gestion d'assurance automobile**, depuis l'inscription d'un assuré jusqu'à la gestion des contrats, des sinistres, de la facturation et des renouvellements.

L'application propose une plateforme accessible à trois catégories d'utilisateurs :

- **Client :** gestion des véhicules, souscription, déclaration de sinistres et consultation des documents.
- **Agent :** vérification et traitement des demandes, gestion des contrats et suivi des dossiers.
- **Administrateur :** administration des utilisateurs et supervision des opérations.

AUXIDRIVE repose sur une architecture client-serveur utilisant **Angular pour le frontend**, **Spring Boot pour le backend** et **MySQL pour la base de données**.

> ⚠️ **Avertissement :** AUXIDRIVE est actuellement une version de développement. Certaines fonctionnalités restent à implémenter et quelques bugs sont encore présents. Cette version ne représente pas la version finale de l'application.

---

## 🎯 Objectifs du projet

Le développement d'AUXIDRIVE poursuit plusieurs objectifs :

- Digitaliser les opérations liées à l'assurance automobile.
- Centraliser les informations des assurés et de leurs véhicules.
- Simplifier les procédures d'inscription et de souscription.
- Faciliter la gestion et le suivi des contrats d'assurance.
- Améliorer le traitement des déclarations de sinistres.
- Automatiser certaines opérations administratives.
- Faciliter le suivi des échéances et des renouvellements.
- Améliorer la communication entre les assurés et les agents.
- Renforcer la traçabilité des différentes opérations.
- Proposer une interface moderne, intuitive et responsive.

---

## ✨ Fonctionnalités principales

### 👤 1. Espace Client

L'espace client permet à un assuré d'effectuer différentes démarches d'assurance automobile en ligne.

#### Inscription et authentification
- Soumission d'une demande d'inscription.
- Saisie des informations personnelles.
- Constitution du dossier client.
- Validation du compte par un agent.
- Réception des identifiants de connexion par email.
- Connexion à un espace personnel.
- Consultation et modification du profil.

#### Gestion des véhicules
- Ajout de véhicules.
- Enregistrement des caractéristiques techniques.
- Consultation des véhicules enregistrés.
- Association des véhicules aux contrats d'assurance.

#### Gestion des contrats
- Demande de souscription en ligne.
- Sélection d'une formule d'assurance.
- Souscription à une offre monovéhicule.
- Consultation des contrats.
- Suivi du statut des demandes.
- Demande de renouvellement.

#### Gestion des sinistres
- Déclaration d'un nouveau sinistre.
- Sélection du véhicule concerné.
- Saisie des informations relatives à l'incident.
- Ajout de photos et de pièces justificatives.
- Consultation des déclarations effectuées.
- Suivi de l'état d'avancement du dossier.

#### Facturation et notifications
- Consultation des factures.
- Suivi des informations de facturation.
- Réception de notifications par email.

---

### 🧑‍💼 2. Espace Agent

L'espace agent permet aux collaborateurs du service d'assurance de traiter les différentes demandes des clients.

#### Gestion des inscriptions
- Consultation des nouvelles demandes.
- Vérification des informations personnelles.
- Contrôle des pièces justificatives.
- Validation ou refus des dossiers.
- Précision du motif en cas de refus.
- Activation des comptes après validation.

#### Gestion des contrats
- Consultation des demandes de souscription.
- Vérification des véhicules concernés.
- Calcul des primes d'assurance.
- Traitement et validation des contrats.
- Gestion des demandes de renouvellement.
- Suivi des échéances contractuelles.

#### Gestion des sinistres
- Consultation des déclarations.
- Vérification des informations transmises.
- Consultation des documents justificatifs.
- Traitement des dossiers de sinistre.
- Suivi des différentes étapes de traitement.
- Gestion des informations liées aux indemnisations.

#### Gestion administrative
- Consultation des assurés.
- Suivi des dossiers.
- Gestion des factures.
- Consultation des notifications.

---

### 🛡️ 3. Espace Administrateur

L'administrateur intervient dans la gestion globale et la supervision de l'application.

Ses principales fonctionnalités comprennent :

- Gestion des comptes utilisateurs.
- Gestion des rôles et autorisations.
- Consultation des informations administratives.
- Supervision des différentes opérations.
- Administration des paramètres de l'application.

---

## 🚧 État actuel du projet

> **AUXIDRIVE est fonctionnel sur plusieurs modules, mais reste en cours de développement et d'amélioration. La version disponible sur GitHub n'est pas définitive.**

### ✅ Fonctionnalités déjà développées

Les principaux modules actuellement développés comprennent :

- Inscription et authentification.
- Gestion des assurés.
- Gestion des véhicules.
- Souscription aux contrats d'assurance.
- Calcul des primes.
- Gestion des contrats.
- Déclaration et suivi des sinistres.
- Facturation.
- Renouvellement des contrats.
- Notifications par email.
- Gestion des utilisateurs selon leurs rôles.

Ces fonctionnalités constituent le socle fonctionnel de l'application. Leur présence ne signifie pas qu'elles sont totalement exemptes d'anomalies.

### ⏳ Fonctionnalité non implémentée

**La résiliation des contrats d'assurance n'est pas encore implémentée.**

Dans la version actuelle, le cycle de vie du contrat couvre principalement la souscription, la gestion et le renouvellement.

Le développement de la résiliation est prévu dans les prochaines évolutions afin de permettre :

- La soumission d'une demande de résiliation.
- La vérification des conditions de résiliation.
- Le traitement et la validation par un agent.
- La mise à jour du statut du contrat.
- La conservation de l'historique des résiliations.

Ces éléments constituent des fonctionnalités envisagées et ne sont pas encore disponibles.

### 🐛 Bugs et limitations

Malgré la présence de plusieurs modules fonctionnels, **certains bugs et imperfections subsistent dans la version actuelle**.

Des améliorations restent nécessaires concernant :

- La correction des anomalies fonctionnelles.
- La stabilité générale de l'application.
- La gestion des erreurs et des cas particuliers.
- La validation de certaines données.
- L'optimisation de certaines interfaces.
- L'amélioration des performances.
- Le renforcement de la sécurité.
- L'approfondissement des tests.

La liste ci-dessus présente les principaux axes d'amélioration et ne constitue pas un inventaire exhaustif des bugs.

### 📌 Statut de développement

| Élément | Statut |
|---|---|
| Architecture frontend/backend | ✅ Développée |
| Authentification et gestion des utilisateurs | ✅ Développées |
| Gestion des véhicules | ✅ Développée |
| Souscription | ✅ Développée |
| Calcul des primes | ✅ Développé |
| Gestion des contrats | ✅ Développée |
| Gestion des sinistres | ✅ Développée |
| Facturation | ✅ Développée |
| Renouvellement | ✅ Développé |
| Notifications email | ✅ Développées |
| Résiliation des contrats | ❌ Non implémentée |
| Correction de l'ensemble des bugs | 🔄 En cours |
| Optimisation et stabilisation | 🔄 À poursuivre |
| Version finale | ⏳ Non publiée |

**Remarque :** le statut « Développé » indique que le module a été implémenté, et non qu'il est entièrement finalisé ou exempt de bugs.

---

## 🛠️ Technologies utilisées

### Frontend

| Technologie | Utilisation |
|---|---|
| Angular 21 | Framework frontend |
| TypeScript | Logique des interfaces |
| HTML5 | Structure des pages |
| CSS3 | Styles et animations |
| Bootstrap | Interfaces responsive |

### Backend

| Technologie | Utilisation |
|---|---|
| Java 25 | Langage backend |
| Spring Boot 4.1.1 | Framework backend |
| Spring Web | Création des API REST |
| Spring Data JPA | Accès et persistance des données |
| Hibernate | Mapping objet-relationnel |
| Spring Security | Sécurité et contrôle d'accès |
| JavaMailSender | Envoi des courriers électroniques |
| Maven | Gestion des dépendances |

### Base de données

| Technologie | Utilisation |
|---|---|
| MySQL | Base de données relationnelle |
| JPA / Hibernate | Gestion de la persistance |

### Outils de développement

- Visual Studio Code
- Git
- GitHub
- Postman
- XAMPP / MySQL
- Angular CLI
- Maven

---

## 🏗️ Architecture de l'application

AUXIDRIVE adopte une architecture client-serveur reposant sur une communication via des **API REST**.

```text
                  UTILISATEURS
            Client / Agent / Admin
                        |
                        v
             +--------------------+
             |      FRONTEND      |
             |       Angular      |
             |  HTML / CSS / TS   |
             +--------------------+
                        |
                    HTTP / JSON
                        |
                        v
             +--------------------+
             |       BACKEND      |
             |     Spring Boot    |
             |                    |
             |     Controller     |
             |         |          |
             |       Service      |
             |         |          |
             |     Repository     |
             +--------------------+
                        |
                    JPA / JDBC
                        |
                        v
             +--------------------+
             |    BASE DE DONNÉES |
             |        MySQL       |
             +--------------------+
```

### Organisation du backend

Le backend est organisé en plusieurs packages :

- `controller` : gestion des requêtes HTTP et des réponses de l'API.
- `service` : logique métier de l'application.
- `model` : représentation des entités.
- `repository` : accès aux données avec Spring Data JPA.
- `config` : configuration de l'application et de la sécurité.

Cette séparation facilite la maintenance, l'évolution et la compréhension du code source.

---

## 📂 Structure du dépôt

```text
AUXIDRIVE/
│
├── Backend/
│   └── Application Spring Boot
│
├── Frontend/
│   └── AUXIDRIVE/
│       └── Application Angular
│
├── Sources/
│   └── Ressources du projet
│
├── .gitignore
│
└── README.md
```

---

## 🚀 Installation et exécution locale

### 1. Prérequis

Les outils suivants sont nécessaires :

- Git
- Java JDK 25
- Node.js compatible avec Angular 21
- npm
- Angular CLI compatible
- MySQL
- Maven ou Maven Wrapper

### 2. Cloner le dépôt

```bash
git clone https://github.com/LaithSama/AUXIDRIVE.git
```

Accéder au dossier :

```bash
cd AUXIDRIVE
```

### 3. Configurer MySQL

Démarrer le serveur MySQL puis créer la base de données.

Exemple :

```sql
CREATE DATABASE auxidrive;
```

Configurer ensuite la connexion dans le fichier `application.properties` du backend.

Exemple de configuration :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auxidrive
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

Les paramètres doivent être adaptés à votre configuration locale.

La valeur `ddl-auto=update` est présentée ici pour le développement et ne constitue pas une stratégie de migration recommandée en production.

### 4. Exécuter le backend

Depuis la racine du dépôt :

```bash
cd Backend
```

Lancer Spring Boot :

```bash
mvn spring-boot:run
```

Le port dépend de la configuration de l'application. Par défaut, Spring Boot utilise généralement le port `8080`.

#### Stockage des PDF de factures

Les PDF sont des fichiers de données, **non versionnés** (`Backend/factures/` est ignoré par Git).
Par défaut, ils sont créés dans `factures/` relativement au répertoire de lancement du backend
(avec `cd Backend`, il s'agit de `Backend/factures/`). Pour une installation durable, définir
`app.factures.dir` dans la configuration du backend, ou la variable d'environnement `APP_FACTURES_DIR`,
avec un **chemin absolu vers un dossier persistant**, hors du dépôt, accessible en écriture :

```properties
app.factures.dir=C:/Dossier/auxidrive-data/factures
```

Le dossier est créé automatiquement à l'émission d'une facture. Sauvegarder **la base de données et
les PDF ensemble** : chaque facture conserve en base le chemin absolu du fichier émis. Changer le
dossier de sortie ne déplace pas les anciens PDF et ne modifie pas leurs chemins enregistrés ;
restaurer les fichiers d'origine à leurs chemins historiques (ou migrer les chemins en base après
sauvegarde) si les téléchargements des anciennes factures renvoient une erreur 404.

Pour récupérer les PDF historiques retirés du dépôt au commit `3cbba63`, uniquement si le dossier
local ne contient aucun PDF plus récent à préserver, lancer depuis la racine du dépôt :

```bash
git restore --source=3cbba63^ --worktree -- Backend/factures
```

Les fichiers restaurés restent ignorés par Git ; cette commande ne restaure pas les PDF créés
après ce commit sur d'autres machines.

### 5. Exécuter le frontend

Ouvrir un autre terminal depuis la racine du dépôt :

```bash
cd Frontend/AUXIDRIVE
```

Installer les dépendances :

```bash
npm install
```

Lancer l'application Angular :

```bash
ng serve
```

Accéder à l'interface :

```text
http://localhost:4200
```

### 6. Configuration des emails

AUXIDRIVE utilise `JavaMailSender` pour envoyer certaines notifications.

Exemple de configuration SMTP :

```properties
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
```

Les paramètres SMTP doivent être configurés avant d'utiliser les fonctionnalités nécessitant l'envoi d'emails.

**Important : ne jamais publier les identifiants SMTP ou les mots de passe réels sur GitHub.**

### 7. Stockage des documents

Les documents et pièces justificatives sont actuellement enregistrés dans des dossiers locaux du backend.

Il est nécessaire de vérifier :

- Les chemins de stockage configurés.
- Les autorisations de lecture et d'écriture.
- La présence des dossiers nécessaires.
- La validation des fichiers importés.

Cette méthode convient à une utilisation locale ou à un environnement de développement, mais nécessite des adaptations pour un déploiement en production.

---

## 🔐 Sécurité

AUXIDRIVE utilise Spring Security pour la gestion de mécanismes de sécurité au niveau backend.

Les principaux aspects concernés sont :

- L'authentification des utilisateurs.
- La gestion des rôles.
- Le contrôle des accès aux fonctionnalités.
- La protection des opérations sensibles.
- La validation des données transmises.

Des améliorations et vérifications supplémentaires restent nécessaires avant toute utilisation en production, notamment pour la protection des données personnelles, des documents justificatifs et des identifiants.

---

## 🔄 Processus métier

### 1. Inscription du client

1. Le visiteur accède à la page d'accueil.
2. Il renseigne ses informations personnelles.
3. Il soumet son dossier d'inscription.
4. Un agent consulte et vérifie le dossier.
5. L'agent accepte ou refuse la demande.
6. En cas de validation, le client reçoit ses identifiants par email.
7. Le client se connecte à son espace personnel.

### 2. Souscription d'un contrat

1. Le client se connecte.
2. Il ajoute un véhicule.
3. Il sélectionne une formule d'assurance.
4. Il remplit le formulaire de souscription.
5. Il envoie sa demande.
6. L'agent examine les informations.
7. L'agent traite et valide le contrat.

### 3. Déclaration de sinistre

1. Le client accède à la rubrique Sinistres.
2. Il sélectionne son véhicule.
3. Il renseigne les circonstances de l'incident.
4. Il ajoute les documents justificatifs.
5. Il soumet sa déclaration.
6. L'agent consulte et traite le dossier.
7. Le client suit l'évolution du traitement.

### 4. Renouvellement

1. Le contrat approche de son échéance.
2. Le client consulte les informations de son contrat.
3. Il effectue une demande de renouvellement.
4. L'agent vérifie et traite la demande.
5. Les informations du contrat sont actualisées selon le traitement effectué.

---

## 🧪 Tests et validation

Les principaux parcours fonctionnels à vérifier comprennent :

| Module | Tests fonctionnels |
|---|---|
| Authentification | Connexion et contrôle des rôles |
| Inscription | Soumission et validation des demandes |
| Véhicules | Ajout et consultation |
| Souscription | Création et traitement des demandes |
| Contrats | Consultation et gestion |
| Sinistres | Déclaration et traitement |
| Facturation | Consultation des factures |
| Renouvellement | Traitement des demandes |
| Notifications | Envoi et réception des emails |
| Sécurité | Vérification des autorisations |

**Attention :** certains tests et corrections doivent encore être approfondis. Le projet n'est pas considéré comme entièrement stabilisé.

---

## 📈 Perspectives d'amélioration

Plusieurs améliorations sont envisagées afin de faire évoluer AUXIDRIVE vers une version plus complète et plus stable.

### Fonctionnalités

- Implémenter le module de résiliation des contrats.
- Enrichir les tableaux de bord.
- Améliorer le suivi des échéances.
- Étendre les fonctionnalités de notification.
- Intégrer éventuellement le paiement électronique.

### Technique

- Corriger les bugs encore présents.
- Renforcer la gestion des erreurs.
- Améliorer la validation des données.
- Optimiser les performances du frontend et du backend.
- Renforcer la sécurité.
- Développer des tests automatisés.
- Mettre en place une chaîne CI/CD.

### Déploiement

- Préparer un environnement de production.
- Configurer HTTPS.
- Sécuriser les informations confidentielles.
- Adapter le stockage des documents.
- Mettre en place des sauvegardes de la base de données.
- Stabiliser l'application avant sa publication finale.

---

## 🎓 Contexte académique

| Information | Description |
|---|---|
| Projet | Projet de Fin d'Études |
| Niveau | Licence 3 |
| Spécialité | Génie Logiciel |
| Établissement | ESP-BIG |
| Entreprise d'accueil | AUXIMAD |
| Service concerné | AUXIASSUR |
| Application développée | AUXIDRIVE |
| Domaine | Assurance automobile |

**Intitulé du projet :**

*Conception et développement d'une application web de gestion d'assurance automobile — Cas AUXIASSUR.*

Ce projet constitue une mise en pratique des compétences acquises en analyse et conception de systèmes d'information, développement web, programmation orientée objet, gestion de bases de données et architecture logicielle.

---

## 👨‍💻 Développeur

**RAFALIMANANA Ny Harena Nohavintsoa**

Étudiant en Licence 3 Génie Logiciel à l'ESP-BIG.

Passionné par le développement logiciel, la conception d'applications web et les nouvelles technologies.

- **GitHub :** [LaithSama](https://github.com/LaithSama)
- **Dépôt :** [AUXIDRIVE](https://github.com/LaithSama/AUXIDRIVE)

---

## ⚠️ Avertissement important

Ce dépôt contient une **version de développement non finalisée** d'AUXIDRIVE, réalisée dans le cadre d'un projet académique.

Bien que plusieurs fonctionnalités soient déjà implémentées et opérationnelles, l'application :

- Présente encore certains bugs.
- Nécessite des corrections et des optimisations.
- Ne dispose pas encore du module de résiliation des contrats.
- Doit faire l'objet de tests et de validations supplémentaires.
- N'est pas encore prête pour un déploiement en production.

Le code source est partagé à des fins de présentation du projet, d'apprentissage et d'évolution technique.

---

<p align="center">
  <strong>🚗 AUXIDRIVE</strong>
</p>

<p align="center">
  Digitaliser, centraliser et simplifier la gestion de l'assurance automobile.
</p>

<p align="center">
  <em>Projet de Fin d'Études — Licence 3 Génie Logiciel — ESP-BIG</em>
</p>

<p align="center">
  <strong>Version de développement — Non finalisée</strong>
</p>
