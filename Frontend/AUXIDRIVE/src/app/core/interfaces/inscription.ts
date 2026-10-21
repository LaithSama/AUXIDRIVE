export type StatutInscription = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE';

/** Demande de souscription déposée par un client, instruite par un agent. */
export interface Inscription {
  idInscription: number;
  statutInscription: StatutInscription;
  dateDemande?: string | null;
  dateDecision?: string | null;
  motifRefus?: string | null;
  assure?: {
    idAssure?: number;
    nomAssure?: string | null;
    prenomAssure?: string | null;
    numerosAssure?: string | null;
  } | null;
}
