-- Migration : autoriser le statut CORRECTION_DEMANDEE sur les inscriptions.
-- À exécuter une fois sur une base existante créée avant l'ajout du parcours
-- de correction des pièces. Sauvegarder la table avant toute modification.
--
-- MySQL conserve la règle NULL / NOT NULL d'origine : la colonne est NULLable
-- dans le schéma actuel. Adapter la clause DEFAULT si votre schéma diffère.

ALTER TABLE inscriptions
    MODIFY COLUMN statut_inscription
    ENUM('EN_ATTENTE', 'REFUSEE', 'VALIDEE', 'CORRECTION_DEMANDEE') DEFAULT NULL;

-- Un même jeton de correction ne doit jamais être partagé par deux dossiers.
-- Ignorer cette instruction si l'index existe déjà.
ALTER TABLE inscriptions
    ADD UNIQUE KEY uk_inscriptions_jeton_correction (jeton_correction_hash);
