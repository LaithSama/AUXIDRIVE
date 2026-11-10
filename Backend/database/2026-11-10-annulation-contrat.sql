-- À exécuter une fois sur une base MySQL existante avant d'annuler un contrat.
-- Hibernate (ddl-auto=update) ajoutera date_annulation et motif_annulation.
-- Sauvegarder la table avant cette modification ; si statut_contrat est déjà
-- en VARCHAR, cette migration n'est pas nécessaire.

ALTER TABLE contrats
    MODIFY COLUMN statut_contrat
    ENUM('EN_ATTENTE', 'VALIDE', 'REFUSE', 'ANNULE') DEFAULT NULL;
