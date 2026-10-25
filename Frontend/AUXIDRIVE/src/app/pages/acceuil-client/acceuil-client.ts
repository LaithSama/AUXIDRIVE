import { HttpClient } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router'; // 👈 Ajout de RouterLink ici
import { Offre } from '../../core/interfaces/offre';

const API = 'http://localhost:8080/api';

/** Offres affichées si l'API ne répond pas (même contenu que les données de départ du backend). */
const OFFRES_PAR_DEFAUT: Offre[] = [
  {
    libelleOffre: 'Monovéhicule', typeOffre: 'MONOVEHICULE', dureeMinMois: 1, nbVehiculesMin: 1, tauxRemise: 0,
    description: ''
  },
  {
    libelleOffre: 'Flotte', typeOffre: 'FLOTTE', dureeMinMois: 12, nbVehiculesMin: 3, tauxRemise: 5,
    description: ''
  },
];

@Component({
  selector: 'app-acceuil-client',
  imports: [FormsModule, RouterLink], // 👈 Ajout de RouterLink ici pour activer les liens du HTML
  templateUrl: './acceuil-client.html',
  styleUrl: './acceuil-client.scss',
})
export class AcceuilClient implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private router = inject(Router);

  /** L'en-tête reçoit une ombre dès que la page est défilée. */
  estScroll = signal(false);
  menuOuvert = signal(false);

  immatriculation = '';
  offres = signal<Offre[]>(OFFRES_PAR_DEFAUT);

  etapes = [
    { titre: 'Vous faites votre demande', texte: 'Remplissez le formulaire en ligne. Les pièces (CIN, carte grise) sont facultatives : vous pouvez aussi tout saisir à la main.' },
    { titre: 'Un agent vérifie votre dossier', texte: 'Il valide votre inscription ou vous explique pourquoi elle est refusée.' },
    { titre: 'Vous recevez vos identifiants', texte: 'Votre compte est créé et votre mot de passe vous arrive par e-mail.' },
    { titre: 'Vous souscrivez votre contrat', texte: 'Ajoutez vos véhicules, choisissez la durée, consultez le prix avant de confirmer.' },
  ];

  espaceClient = [
    'Suivre vos contrats, leurs dates d\'échéance et votre bonus-malus',
    'Télécharger vos factures en PDF',
    'Ajouter ou modifier vos véhicules',
    'Déclarer un sinistre et suivre son traitement',
  ];

  horaires = [
    { jour: 'Lundi à Vendredi', heures: '08h00 – 17h00' },
    { jour: 'Samedi', heures: '08h00 – 12h00' },
  ];

  coordonnees = [
    'Galerie Zoom Ankorondrano, près du Restaurant « Au QG » et de l\'Institut Pasteur',
    'Rez-de-chaussée bâtiment AUXIMAD, Antsahavola',
  ];

  telephones = [
    { affiche: '032 05 242 21', lien: 'tel:+261320524221' },
    { affiche: '032 05 243 32', lien: 'tel:+261320524332' },
  ];

  reflexes = [
    'Sécurisez les lieux',
    'Vérifiez les dégâts',
    'Remplissez votre constat amiable',
    'Contactez votre assurance rapidement',
  ];

  ngOnInit(): void {
    // le catalogue d'offres est public (GET /api/offres)
    this.http.get<Offre[]>(`${API}/offres`).subscribe({
      next: (liste) => { if (liste.length) { this.offres.set(liste); } },
      error: () => { /* on garde les offres par défaut */ },
    });

    // ombre de l'en-tête au scroll
    this.surScroll();
    window.addEventListener('scroll', this.surScroll, { passive: true });
  }

  ngOnDestroy(): void {
    window.removeEventListener('scroll', this.surScroll);
  }

  private surScroll = (): void => {
    this.estScroll.set(window.scrollY > 10);
  };

  /** Met la saisie en majuscules, sans caractères autres que lettres, chiffres et espaces. */
  normaliser(valeur: string): void {
    this.immatriculation = valeur.toUpperCase().replace(/[^A-Z0-9 ]/g, '').slice(0, 10);
  }

  commencer(): void {
    const q = this.immatriculation.trim();
    this.router.navigate(['/inscription'], { queryParams: q ? { immatriculation: q } : {} });
  }

  details(o: Offre): string {
    if (o.typeOffre === 'MONOVEHICULE') {
      return 'Un seul véhicule, couvert de 1 à 12 mois.';
    }
    return `À partir de ${o.nbVehiculesMin ?? 3} véhicules, pour ${o.dureeMinMois ?? 12} mois au minimum.`;
  }

  remise(o: Offre): string | null {
    return o.typeOffre === 'FLOTTE' && o.tauxRemise
      ? `${o.tauxRemise} % de remise dès 5 véhicules`
      : null;
  }
}
