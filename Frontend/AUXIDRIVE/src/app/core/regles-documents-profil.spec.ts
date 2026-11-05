import { describe, expect, it } from 'vitest';
import { DocumentProfil, TypeDocumentProfil } from './interfaces/document-profil';
import { carteRoseIncomplete, cinSatisfait, dossierComplet, suppressionAvertir, suppressionCinInterdite } from './regles-documents-profil';

const piece = (typeDocument: TypeDocumentProfil, statutDocuments: DocumentProfil['statutDocuments'] = 'VALIDE'): DocumentProfil => ({
  idDocument: 1, typeDocument, statutDocuments, formatFichier: 'pdf', tailleKo: 10,
  dateTeleversement: null, motifRejet: null, dateValidation: null, verrouille: false,
});

describe('règles des documents du profil', () => {
  it('exige une CIN seulement en l’absence de numéro et de pièce utilisable', () => {
    expect(cinSatisfait([], false)).toBe(false);
    expect(cinSatisfait([], true)).toBe(true);
    expect(cinSatisfait([piece('CIN', 'EN_ATTENTE')], false)).toBe(true);
    expect(cinSatisfait([piece('CIN', 'REJETE')], false)).toBe(false);
  });

  it('accepte zéro document véhicule, la carte grise, le modèle N°1 et leurs combinaisons', () => {
    for (const documents of [
      [], [piece('CARTE_GRISE')], [piece('MODELE_N1')],
      [piece('CARTE_GRISE'), piece('CARTE_ROSE')],
      [piece('MODELE_N1'), piece('CARTE_ROSE')],
      [piece('CARTE_GRISE'), piece('MODELE_N1'), piece('CARTE_ROSE')],
    ]) {
      expect(carteRoseIncomplete(documents)).toBe(false);
      expect(dossierComplet(documents, true)).toBe(true);
    }
    expect(carteRoseIncomplete([piece('CARTE_ROSE')])).toBe(true);
    expect(dossierComplet([piece('CARTE_ROSE')], true)).toBe(false);
    expect(carteRoseIncomplete([piece('CARTE_ROSE'), piece('CARTE_GRISE', 'REJETE')])).toBe(true);
  });

  it('bloque la suppression de la seule CIN et avertit si la carte rose devient seule', () => {
    expect(suppressionCinInterdite('CIN', false)).toBe(true);
    expect(suppressionCinInterdite('CIN', true)).toBe(false);
    expect(suppressionAvertir([piece('CARTE_ROSE'), piece('CARTE_GRISE')], 'CARTE_GRISE')).toBe(true);
    expect(suppressionAvertir([piece('CARTE_ROSE'), piece('CARTE_GRISE'), piece('MODELE_N1')], 'CARTE_GRISE')).toBe(false);
    expect(suppressionAvertir([piece('CARTE_ROSE'), piece('MODELE_N1')], 'MODELE_N1')).toBe(true);
    expect(suppressionAvertir([piece('CARTE_ROSE')], 'CARTE_ROSE')).toBe(false);
  });
});
