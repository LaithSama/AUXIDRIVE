
# 🚗 AUXIDRIVE
### Application Web de Gestion d'Assurance Automobile

![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-Frontend-7952B3?logo=bootstrap&logoColor=white)
![Status](https://img.shields.io/badge/Statut-Projet_académique-blue)

---

## 📖 Présentation du projet

**AUXIDRIVE** est une application web de gestion d'assurance automobile conçue dans le cadre d'un projet de fin d'études (PFE) en Génie Logiciel.

Le projet porte sur la **conception et le développement d'une application web de gestion d'assurance automobile**, appliquée au service **AUXIASSUR** de l'entreprise **AUXIMAD**.

L'application vise à digitaliser et centraliser les principales opérations liées à l'assurance automobile, depuis l'inscription d'un assuré jusqu'au suivi des contrats, des sinistres et de la facturation.

Elle propose une plateforme accessible aux clients, aux agents d'assurance et aux administrateurs, afin de simplifier les démarches administratives et d'améliorer le suivi des opérations.

### 🎯 Objectifs

- Digitaliser les processus de gestion d'assurance automobile.
- Centraliser les informations relatives aux assurés et à leurs véhicules.
- Simplifier les demandes de souscription et de renouvellement.
- Faciliter la déclaration et le suivi des sinistres.
- Automatiser certaines opérations administratives.
- Améliorer la traçabilité des contrats et des factures.
- Renforcer la communication entre les clients et les agents d'assurance.
- Proposer une interface moderne, intuitive et responsive.

---

## ✨ Fonctionnalités principales

### 👤 Espace Client

L'espace client permet aux assurés d'accéder aux services d'assurance automobile à partir d'une interface dédiée.

**Gestion du compte**
- Demande d'inscription en ligne.
- Saisie des informations personnelles.
- Constitution du dossier d'inscription.
- Authentification sécurisée.
- Consultation et modification du profil.

**Gestion des véhicules**
- Ajout et consultation des véhicules.
- Enregistrement des informations techniques.
- Association des véhicules aux contrats d'assurance.

**Gestion des contrats**
- Demande de souscription en ligne.
- Choix d'une formule d'assurance.
- Consultation des contrats.
- Suivi du statut des demandes.
- Demande de renouvellement.

**Gestion des sinistres**
- Déclaration d'un sinistre.
- Sélection du véhicule concerné.
- Saisie des informations relatives à l'incident.
- Transmission de documents et de photographies.
- Consultation de l'état d'avancement du dossier.

**Facturation et notifications**
- Consultation des factures.
- Suivi des informations de facturation.
- Réception de notifications relatives aux opérations.

### 🧑‍💼 Espace Agent

L'espace agent est destiné aux collaborateurs chargés du traitement des dossiers d'assurance.

**Gestion des assurés**
- Consultation des demandes d'inscription.
- Vérification des informations transmises.
- Validation ou refus d'une demande.
- Gestion et suivi des dossiers clients.

**Gestion des contrats**
- Consultation des demandes de souscription.
- Vérification des informations du véhicule.
- Traitement des demandes.
- Calcul et gestion des primes d'assurance.
- Validation des contrats.
- Gestion des renouvellements.

**Gestion des sinistres**
- Consultation des déclarations.
- Vérification des informations et des pièces justificatives.
- Traitement et suivi des dossiers.
- Gestion des informations relatives aux indemnisations.

**Gestion administrative**
- Suivi des factures.
- Consultation des informations des assurés.
- Notifications liées aux différentes opérations.

### 🛡️ Espace Administrateur

L'administrateur intervient dans la supervision globale de l'application.

Ses principales responsabilités comprennent :

- Gestion des comptes utilisateurs.
- Gestion des rôles et des autorisations.
- Supervision des opérations.
- Consultation des données de gestion.
- Administration des paramètres de l'application.

---

## 🛠️ Technologies utilisées

AUXIDRIVE repose sur une architecture web composée d'un frontend, d'un backend et d'une base de données relationnelle.

### Frontend

| Technologie | Utilisation |
|---|---|
| Angular 21 | Framework frontend |
| TypeScript | Langage de programmation |
| HTML5 | Structure des interfaces |
| CSS3 | Mise en forme |
| Bootstrap | Composants responsive |

### Backend

| Technologie | Utilisation |
|---|---|
| Java 25 | Langage backend |
| Spring Boot 4.1.1 | Framework backend |
| Spring Web | Développement des API REST |
| Spring Data JPA | Persistance des données |
| Spring Security | Authentification et sécurité |
| JavaMailSender | Envoi des emails |
| Maven | Gestion des dépendances |

### Base de données

| Technologie | Utilisation |
|---|---|
| MySQL | Base de données relationnelle |
| JPA / Hibernate | Mapping objet-relationnel |

### Outils de développement

- Visual Studio Code
- Git et GitHub
- Postman
- XAMPP / MySQL
- Maven
- Angular CLI

---

## 🏗️ Architecture de l'application

AUXIDRIVE utilise une architecture client-serveur reposant sur des API REST.

```text
            UTILISATEURS
       (Client / Agent / Admin)
                  |
                  v
        +-------------------+
        |     FRONTEND      |
        |     Angular       |
        |  HTML / CSS / TS  |
        +-------------------+
                  |
             HTTP / JSON
                  |
                  v
        +-------------------+
        |      BACKEND      |
        |    Spring Boot    |
        |                   |
        |    Controller     |
        |        |          |
        |      Service      |
        |        |          |
        |    Repository     |
        +-------------------+
                  |
               JPA
                  |
                  v
        +-------------------+
        |    DATABASE       |
        |      MySQL        |
        +-------------------+
```

### Organisation du backend

Le backend est structuré autour de plusieurs packages :

- **controller** : réception et traitement des requêtes HTTP.
- **service** : implémentation de la logique métier.
- **model** : définition des entités de l'application.
- **repository** : accès aux données avec Spring Data JPA.
- **config** : configuration et sécurité de l'application.

Cette organisation permet de séparer les responsabilités et de faciliter la maintenance du code.

---

## 📂 Structure du projet

Le dépôt GitHub est organisé de la manière suivante :

```text
AUXIDRIVE/
|
|-- Backend/
|   |-- Code source Spring Boot
|   |-- Configuration Maven
|
|-- Frontend/
|   |-- AUXIDRIVE/
|       |-- Code source Angular
|       |-- Configuration Angular
|
|-- Sources/
|   |-- Ressources du projet
|
|-- .gitignore
|
|-- README.md
```

Le frontend communique avec le backend au moyen de requêtes HTTP vers les endpoints de l'API REST.

---

## 🚀 Installation et exécution locale

### 1. Prérequis

Avant de lancer AUXIDRIVE, assurez-vous d'avoir installé :

- Git
- Java JDK 25
- Node.js et npm compatibles avec Angular 21
- Angular CLI compatible
- MySQL
- Maven ou le Maven Wrapper du projet

### 2. Cloner le dépôt

```bash
git clone https://github.com/LaithSama/AUXIDRIVE.git
```

Accéder au répertoire :

```bash
cd AUXIDRIVE
```

### 3. Configurer la base de données

Démarrer MySQL puis créer une base de données :

```sql
CREATE DATABASE auxidrive;
```

Adapter ensuite la configuration Spring Boot à votre environnement.

Exemple de configuration à adapter dans `application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auxidrive
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

**Important :** les noms de base de données, identifiants et paramètres doivent correspondre à la configuration effective de votre installation.

L'utilisation de `ddl-auto=update` est réservée ici au développement local.

### 4. Démarrer le backend

Accéder au répertoire backend :

```bash
cd Backend
```

Lancer Spring Boot avec Maven :

```bash
mvn spring-boot:run
```

Si le Maven Wrapper est présent, il peut être utilisé à la place de Maven installé globalement.

Le port de l'API dépend de la configuration Spring Boot.

### 5. Démarrer le frontend

Depuis la racine du dépôt :

```bash
cd Frontend/AUXIDRIVE
```

Installer les dépendances :

```bash
npm install
```

Lancer Angular :

```bash
ng serve
```

Ouvrir ensuite :

```text
http://localhost:4200
```

Le frontend et le backend doivent être correctement configurés pour communiquer entre eux.

### 6. Configuration SMTP

L'application utilise JavaMailSender pour l'envoi de courriers électroniques.

La configuration SMTP doit être renseignée dans l'environnement du backend.

Exemple :

```properties
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
```

Ne jamais publier les mots de passe SMTP ou les identifiants de production dans le dépôt GitHub.

### 7. Stockage des documents

Les pièces justificatives et les documents transmis par les utilisateurs sont stockés dans des répertoires du backend.

Avant l'exécution, vérifier :

- La configuration des chemins de stockage.
- L'existence des répertoires nécessaires.
- Les autorisations de lecture et d'écriture.
- Les règles de validation des fichiers transmis.

---

## 🔐 Sécurité

La sécurité représente un aspect essentiel d'une application manipulant des données personnelles et des informations d'assurance.

AUXIDRIVE s'appuie notamment sur Spring Security pour organiser les mécanismes d'authentification et de contrôle d'accès.

Les principales préoccupations de sécurité sont :

- Authentification des utilisateurs.
- Gestion des rôles client, agent et administrateur.
- Protection des opérations sensibles.
- Contrôle des accès aux ressources.
- Validation des informations transmises.
- Protection des identifiants et des paramètres sensibles.

Le déploiement en production nécessite également une configuration HTTPS, une protection appropriée des documents et une gestion sécurisée des secrets.

---

## 🔄 Exemple de processus métier

### Processus d'inscription

1. Un visiteur accède à la page d'accueil.
2. Il remplit le formulaire d'inscription.
3. Il transmet les informations de son dossier.
4. Un agent consulte la demande.
5. L'agent vérifie les informations.
6. Il valide ou refuse la demande avec un motif.
7. En cas de validation, le client reçoit ses identifiants par email.
8. Le client peut accéder à son espace personnel.

### Processus de souscription

1. Le client se connecte à son espace.
2. Il enregistre son véhicule.
3. Il accède au module de souscription.
4. Il sélectionne la formule souhaitée.
5. Il transmet sa demande.
6. L'agent vérifie et traite le dossier.
7. Le contrat est établi après validation.

### Processus de déclaration de sinistre

1. Le client accède au module Sinistres.
2. Il sélectionne le véhicule concerné.
3. Il renseigne les circonstances du sinistre.
4. Il ajoute les pièces justificatives nécessaires.
5. Il transmet sa déclaration.
6. L'agent prend en charge le dossier.
7. Le client peut consulter l'évolution du traitement.

---

## 🧪 Tests et validation

Les vérifications fonctionnelles portent notamment sur :

| Module | Vérification |
|---|---|
| Authentification | Accès aux espaces utilisateurs |
| Inscription | Soumission et validation des dossiers |
| Véhicules | Ajout et consultation |
| Contrats | Demande et traitement des souscriptions |
| Sinistres | Déclaration et suivi |
| Facturation | Consultation des informations |
| Notifications | Transmission des notifications |
| Sécurité | Contrôle des accès selon les rôles |

Les essais fonctionnels permettent de vérifier le comportement des différentes opérations et les échanges entre le frontend, le backend et la base de données.

---

## 📈 Perspectives d'amélioration

Plusieurs évolutions peuvent être envisagées :

- Déploiement sur une infrastructure de production.
- Intégration du paiement électronique.
- Amélioration des tableaux de bord et statistiques.
- Automatisation avancée du suivi des échéances.
- Développement d'une application mobile.
- Gestion complète de la résiliation des contrats.
- Mise en place de tests automatisés et d'un pipeline CI/CD.
- Migration du stockage local des documents vers un service de stockage sécurisé.

Ces fonctionnalités constituent des pistes d'évolution et ne doivent pas être considérées comme déjà implémentées.

---

## 🎓 Contexte académique

**Type de projet :** Projet de Fin d'Études (PFE)

**Formation :** Licence 3 — Génie Logiciel

**Établissement :** ESP-BIG

**Entreprise d'accueil :** AUXIMAD

**Service concerné :** AUXIASSUR — Assurance automobile

**Nom de l'application :** AUXIDRIVE

**Sujet :** Conception et développement d'une application web de gestion d'assurance automobile.

Ce projet s'inscrit dans une démarche de digitalisation des opérations de gestion, avec pour objectif de proposer une solution informatique adaptée aux besoins du domaine de l'assurance automobile.

---

## 👨‍💻 Développeur

**RAFALIMANANA Ny Harena Nohavintsoa**

Étudiant en Génie Logiciel, intéressé par le développement d'applications web, les architectures logicielles et la conception de solutions informatiques.

**GitHub :** [@LaithSama](https://github.com/LaithSama)

**Projet :** [AUXIDRIVE](https://github.com/LaithSama/AUXIDRIVE)

---

## 📌 État du projet

AUXIDRIVE est un projet académique disposant de fonctionnalités métier développées pour les besoins de la gestion d'assurance automobile.

Le projet reste évolutif et peut faire l'objet d'améliorations techniques, fonctionnelles et de sécurité avant une éventuelle exploitation en production.

---

<p align="center">
  <strong>AUXIDRIVE — Digitaliser et simplifier la gestion de l'assurance automobile.</strong>
</p>
