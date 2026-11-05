import { DocumentProfil, TypeDocumentProfil } from './interfaces/document-profil';

/** Une pièce rejetée doit être remplacée avant de satisfaire une exigence. */
export function pieceUtilisable(documents: DocumentProfil[], type: TypeDocumentProfil): boolean {
  return documents.some(document => document.typeDocument === type && document.statutDocuments !== 'REJETE');
}

export function cinSatisfait(documents: DocumentProfil[], cinRenseigne: boolean): boolean {
  return cinRenseigne || pieceUtilisable(documents, 'CIN');
}

export function carteRoseIncomplete(documents: DocumentProfil[]): boolean {
  return pieceUtilisable(documents, 'CARTE_ROSE')
    && !pieceUtilisable(documents, 'CARTE_GRISE')
    && !pieceUtilisable(documents, 'MODELE_N1');
}

export function dossierComplet(documents: DocumentProfil[], cinRenseigne: boolean): boolean {
  return cinSatisfait(documents, cinRenseigne) && !carteRoseIncomplete(documents);
}

export function suppressionCinInterdite(type: TypeDocumentProfil, cinRenseigne: boolean): boolean {
  return type === 'CIN' && !cinRenseigne;
}

export function suppressionAvertir(documents: DocumentProfil[], type: TypeDocumentProfil): boolean {
  if (type !== 'CARTE_GRISE' && type !== 'MODELE_N1') return false;
  return !carteRoseIncomplete(documents)
    && carteRoseIncomplete(documents.filter(document => document.typeDocument !== type));
}
