import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Profil } from '../../../core/interfaces/profil';
import { DocumentProfil } from '../../../core/interfaces/document-profil';
import { DashboardProfil } from './profil';
import { environment } from '../../../../environments/environment';

const API = `${environment.apiUrl}/api/profil`;

const DOCUMENT: DocumentProfil = {
  idDocument: 1,
  typeDocument: 'CIN',
  formatFichier: 'jpg',
  tailleKo: 120,
  dateTeleversement: '2026-10-01T10:00:00',
  statutDocuments: 'EN_ATTENTE',
  motifRejet: null,
  dateValidation: null,
  verrouille: false,
};

const PROFIL: Profil = {
  compte: { email: 'client@auxi.mg', dateCreation: '2026-01-05T09:00:00', dateSuppressionPlanifiee: null },
  assure: {
    cinRenseigne: true,
    cinMasque: '•••• 7890',
    numerosAssure: 'A-001',
    civilite: 'M.',
    dateNaissance: '1990-05-12',
    nomAssure: 'Ranaivo',
    prenomAssure: 'Hery',
    adresse: 'Antananarivo',
    contact: '034 00 000 00',
    mail: 'client@auxi.mg',
  },
};

describe('DashboardProfil', () => {
  let component: DashboardProfil;
  let fixture: ComponentFixture<DashboardProfil>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardProfil],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardProfil);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const requete = httpMock.expectOne(API);
    expect(requete.request.method).toBe('GET');
    requete.flush(PROFIL);

    const documents = httpMock.expectOne(`${API}/documents`);
    expect(documents.request.method).toBe('GET');
    documents.flush([]);

    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load the profile', () => {
    expect(component).toBeTruthy();
    expect(component.chargement).toBe(false);
    expect(component.nom).toBe('Ranaivo');
    expect(component.prenom).toBe('Hery');
    expect(component.emailConnexion).toBe('client@auxi.mg');
    expect(component.suppressionPlanifiee).toBe(false);
  });

  it('validates the information form and the password form', () => {
    component.nom = '';
    expect(component.informationsValides).toBe(false);
    component.nom = 'Ranaivo';
    expect(component.informationsValides).toBe(true);

    component.mdpActuel = 'ancien';
    component.mdpNouveau = 'court';
    component.mdpConfirmation = 'court';
    expect(component.motDePasseValide).toBe(false);
    component.mdpNouveau = 'abcdefghij';
    component.mdpConfirmation = 'abcdefghij';
    expect(component.motDePasseValide).toBe(true);
    component.mdpConfirmation = 'autre';
    expect(component.motDePasseValide).toBe(false);
  });

  it('enregistrer() sends the updated information', () => {
    component.adresse = 'Toamasina';
    component.enregistrer();

    const requete = httpMock.expectOne((r) => r.url === API && r.method === 'PUT');
    expect(requete.request.body.adresse).toBe('Toamasina');
    requete.flush({ ...PROFIL, assure: { ...PROFIL.assure, adresse: 'Toamasina' } });

    expect(component.messageSucces).toContain('enregistrées');
    expect(component.adresse).toBe('Toamasina');
  });

  it('enregistrer() reports a duplicate login e-mail', () => {
    component.emailConnexion = 'dupe@auxi.mg';
    component.enregistrer();

    const requete = httpMock.expectOne((r) => r.url === API && r.method === 'PUT');
    requete.flush({ message: 'existe' }, { status: 409, statusText: 'Conflict' });

    expect(component.messageErreur).toContain('déjà utilisé');
    expect(component.messageSucces).toBe('');
  });

  it('plans then cancels the account deletion', () => {
    component.planifierSuppression();
    const planifiee = httpMock.expectOne(`${API}/suppression`);
    expect(planifiee.request.method).toBe('POST');
    planifiee.flush({ dateSuppressionPlanifiee: '2026-10-11T10:00:00' });

    expect(component.suppressionPlanifiee).toBe(true);
    expect(component.dateSuppressionFr).toContain('octobre');

    component.annulerSuppression();
    const annulee = httpMock.expectOne(`${API}/suppression/annuler`);
    annulee.flush({ dateSuppressionPlanifiee: null });

    expect(component.suppressionPlanifiee).toBe(false);
    expect(component.messageSucces).toContain('annulée');
  });

  it('ajoute une pièce manquante et la place en attente de vérification', () => {
    const fichier = new File(['contenu'], 'cin.jpg', { type: 'image/jpeg' });
    const entree = { files: [fichier], value: 'cin.jpg' } as unknown as HTMLInputElement;

    component.choisirFichier('CIN', { target: entree } as unknown as Event);

    const requete = httpMock.expectOne((r) => r.url === `${API}/documents` && r.method === 'POST');
    const corps = requete.request.body as FormData;
    expect(corps.get('type')).toBe('CIN');
    expect((corps.get('fichier') as File).name).toBe('cin.jpg');
    requete.flush(DOCUMENT);

    expect(component.documents.length).toBe(1);
    expect(component.documents[0].statutDocuments).toBe('EN_ATTENTE');
    expect(component.messageDocuments).toContain('ajoutée');
  });

  it('refuse localement un fichier trop volumineux sans appeler le serveur', () => {
    const tropGros = { name: 'scan.pdf', size: 6 * 1024 * 1024 } as File;
    const entree = { files: [tropGros], value: 'scan.pdf' } as unknown as HTMLInputElement;

    component.choisirFichier('CARTE_GRISE', { target: entree } as unknown as Event);

    expect(component.erreurDocument).toContain('5 Mo maximum');
    httpMock.expectNone(`${API}/documents`);
  });

  it('supprime une pièce après confirmation', () => {
    component.documents = [DOCUMENT];
    component.demanderSuppression(DOCUMENT);
    expect(component.documentASupprimer).toBe(DOCUMENT);

    component.confirmerSuppressionDocument();
    const requete = httpMock.expectOne((r) =>
      r.url === `${API}/documents/1` && r.method === 'DELETE');
    requete.flush(null);

    expect(component.documents).toEqual([]);
    expect(component.documentASupprimer).toBeNull();
    expect(component.messageDocuments).toContain('supprimée');
  });

  it('affiche un motif de rejet et bloque les actions d’une pièce verrouillée', () => {
    const rejetee: DocumentProfil = {
      ...DOCUMENT,
      statutDocuments: 'REJETE',
      motifRejet: 'Photo illisible',
      verrouille: true,
    };
    component.documents = [rejetee];

    expect(component.statutLibelle(rejetee)).toBe('À remplacer');
    expect(component.statutClasse(rejetee)).toBe('statut-rejete');
    expect(rejetee.motifRejet).toBe('Photo illisible');
    expect(rejetee.verrouille).toBe(true);
  });

  it('affiche la CIN facultative quand le numéro est enregistré', () => {
    expect(component.cinRenseigne).toBe(true);
    expect(component.badgeType('CIN')).toBe('Facultatif');
    expect(component.aideType('CIN')).toContain('•••• 7890');
    expect(component.documentsComplets).toBe(true);
    fixture.detectChanges();
    const section: HTMLElement = fixture.nativeElement.querySelector('.mes-documents');
    expect(section.textContent).toContain('Identité');
    expect(section.textContent).toContain('Véhicule');
    expect(section.textContent).toContain('Pièces requises fournies');
    expect(section.textContent).toContain('•••• 7890');
  });

  it('exige la CIN sans numéro et refuse sa suppression', () => {
    component.profil = { ...PROFIL, assure: { ...PROFIL.assure, cinRenseigne: false, cinMasque: null } };
    expect(component.badgeType('CIN')).toBe('Obligatoire');
    expect(component.documentsComplets).toBe(false);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.documents-synthese')?.textContent).toContain('CIN à fournir');
    component.documents = [DOCUMENT];
    component.demanderSuppression(DOCUMENT);
    expect(component.documentASupprimer).toBeNull();
    expect(component.erreurDocument).toContain('Suppression impossible');
    httpMock.expectNone(`${API}/documents/1`);
  });

  it('avertit quand une carte rose seule est ajoutée puis lors de la suppression de sa carte grise', () => {
    const rose: DocumentProfil = { ...DOCUMENT, idDocument: 2, typeDocument: 'CARTE_ROSE' };
    component.documents = [rose];
    expect(component.roseIncomplete).toBe(true);
    expect(component.documentsComplets).toBe(false);
    expect(component.badgeType('CARTE_ROSE')).toBe('À compléter');

    const grise: DocumentProfil = { ...DOCUMENT, idDocument: 3, typeDocument: 'CARTE_GRISE' };
    component.documents = [rose, grise];
    expect(component.documentsComplets).toBe(true);
    expect(component.suppressionAvecAvertissement(grise)).toBe(true);
    component.demanderSuppression(grise);
    component.confirmerSuppressionDocument();
    httpMock.expectOne(`${API}/documents/3`).flush(null);
    expect(component.roseIncomplete).toBe(true);
    expect(component.avertissementDocuments).toContain('carte rose');
  });
});
