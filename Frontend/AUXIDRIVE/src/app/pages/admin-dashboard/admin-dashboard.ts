import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Assure } from '../../core/interfaces/assure';
import { CompteClientVue, StatutCompte } from '../../core/interfaces/compte-client';
import { Statistiques } from '../../core/interfaces/statistiques';
import { Role, StatutUtilisateur, Utilisateur } from '../../core/interfaces/utilisateur';
import { DateFrPipe } from '../../core/pipes/date-fr.pipe';
import { AdminService } from '../../services/admin-service';
import { AuthService } from '../../services/auth-service';
import { UtilisateurService } from '../../services/utilisateur-service';
import { ProfilStaffPanel } from '../../components/profil-staff/profil-staff';

type VueAdmin = 'tableau' | 'utilisateurs' | 'comptes' | 'assures' | 'profil';
type SectionModale = 'utilisateur' | 'compte' | 'assure';
type ModeModale = 'CREATION' | 'EDITION';

/** Espace d'administration : tableau de bord + gestion des utilisateurs, comptes clients et assurés. */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [FormsModule, RouterLink, DateFrPipe, ProfilStaffPanel],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard implements OnInit {
  private adminService = inject(AdminService);
  private utilisateurService = inject(UtilisateurService);
  private authService = inject(AuthService);
  private router = inject(Router);

  /** Section affichée dans le contenu principal. */
  vue = signal<VueAdmin>('tableau');

  /** Sidebar dépliable (mobile). */
  sidebarOuverte = signal(false);

  emailAdmin = this.authService.getEmail() ?? 'Administrateur';

  messageSucces = '';
  messageErreur = '';

  // ---------- Tableau de bord ----------
  statistiques = signal<Statistiques | null>(null);

  // ---------- Utilisateurs ----------
  utilisateurs = signal<Utilisateur[]>([]);
  roles = signal<Role[]>([]);

  /** Rôles proposables pour un agent/administrateur (le rôle CLIENT est réservé aux comptes clients). */
  rolesStaff = computed(() => this.roles().filter(r => r.libRole !== 'CLIENT'));

  // Filtres utilisateurs
  rechercheUtilisateur = signal('');
  filtreRoleUtilisateur = signal<'tous' | 'AGENT' | 'ADMIN'>('tous');
  filtreStatutUtilisateur = signal<'tous' | StatutUtilisateur>('tous');
  triUtilisateur = signal<'nomAsc' | 'nomDesc' | 'recent' | 'ancien'>('nomAsc');

  utilisateursAffiches = computed(() => {
    const liste = this.utilisateurs().filter(u => {
      const hay = [u.nomUtil, u.email, u.role.libRole, u.statutUtilisateur]
        .map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheUtilisateur())) return false;
      if (this.filtreRoleUtilisateur() !== 'tous' && u.role.libRole !== this.filtreRoleUtilisateur()) {
        return false;
      }
      if (this.filtreStatutUtilisateur() !== 'tous'
        && u.statutUtilisateur !== this.filtreStatutUtilisateur()) {
        return false;
      }
      return true;
    });
    return this.trierUtilisateurs(liste);
  });

  // ---------- Comptes clients ----------
  comptes = signal<CompteClientVue[]>([]);

  // Filtres comptes clients
  rechercheCompte = signal('');
  filtreStatutCompte = signal<'tous' | StatutCompte>('tous');
  filtreConnexion = signal<'tous' | 'jamais' | 'deja'>('tous');
  triCompte = signal<'nomAsc' | 'emailAsc' | 'recent' | 'ancien'>('nomAsc');

  comptesAffiches = computed(() => {
    const liste = this.comptes().filter(c => {
      const hay = [c.email, c.numerosAssure, c.nomAssure, c.prenomAssure, c.statutCompte]
        .map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheCompte())) return false;
      if (this.filtreStatutCompte() !== 'tous'
        && c.statutCompte !== this.filtreStatutCompte()) {
        return false;
      }
      if (this.filtreConnexion() === 'jamais' && c.dateDerniereConnexion) return false;
      if (this.filtreConnexion() === 'deja' && !c.dateDerniereConnexion) return false;
      return true;
    });
    return this.trierComptes(liste);
  });

  // ---------- Assurés ----------
  assures = signal<Assure[]>([]);

  // Filtres assurés
  rechercheAssure = signal('');
  filtreTypeAssure = signal<'tous' | 'physique' | 'morale'>('tous');
  filtreMailAssure = signal<'tous' | 'avec' | 'sans'>('tous');
  triAssure = signal<'nomAsc' | 'nomDesc' | 'numeroAsc' | 'recent'>('nomAsc');

  assuresAffiches = computed(() => {
    const liste = this.assures().filter(a => {
      const hay = [a.numerosAssure, a.nomAssure, a.prenomAssure, a.mail, a.contact]
        .map(v => this.normaliser(v)).join(' ');
      if (!this.correspond(hay, this.rechercheAssure())) return false;
      if (this.filtreTypeAssure() === 'physique' && a.personneMorale) return false;
      if (this.filtreTypeAssure() === 'morale' && !a.personneMorale) return false;
      if (this.filtreMailAssure() === 'avec' && !a.mail) return false;
      if (this.filtreMailAssure() === 'sans' && a.mail) return false;
      return true;
    });
    return this.trierAssures(liste);
  });

  // ---------- Modale ----------
  sectionModale = signal<SectionModale | null>(null);
  modeModale = signal<ModeModale>('CREATION');

  formUtilisateur = {
    idUtil: null as number | null,
    email: '',
    nomUtil: '',
    motDePasse: '',
    libRole: 'AGENT',
    statutUtilisateur: 'ACTIF' as StatutUtilisateur,
  };

  formCompte = {
    idCompteClient: null as number | null,
    idAssure: null as number | null,
    email: '',
    statutCompte: 'ACTIF',
  };

  formAssure = {
    idAssure: null as number | null,
    numerosAssure: '',
    nomAssure: '',
    prenomAssure: '',
    personneMorale: false,
    dateNaissance: '',
    sexe: '',
    civilite: '',
    nationalite: 'Malgache',
    paysResidence: 'Madagascar',
    adresse: '',
    contact: '',
    mail: '',
  };

  /** Mot de passe temporaire affiché après la création d'un compte client. */
  motDePasseTemporaire = signal<string | null>(null);

  /** false si l'e-mail d'identifiants n'est pas parti : le mot de passe est alors à transmettre à la main. */
  emailEnvoye = signal(true);

  /** Confirmation de suppression en attente. */
  suppression = signal<{ section: SectionModale; id: number; libelle: string } | null>(null);

  ngOnInit(): void {
    this.chargerStatistiques();
    this.chargerUtilisateurs();
    this.chargerComptes();
    this.chargerAssures();
    this.adminService.getRoles().subscribe({
      next: roles => this.roles.set(roles),
      error: err => this.signalerErreur(err, 'Impossible de charger les rôles.'),
    });
  }

  // ------------------------------------------------------------------ chargements

  chargerStatistiques(): void {
    this.adminService.getStatistiques().subscribe({
      next: stats => this.statistiques.set(stats),
      error: err => this.signalerErreur(err, 'Impossible de charger les statistiques.'),
    });
  }

  chargerUtilisateurs(): void {
    this.utilisateurService.getUtilisateurs().subscribe({
      next: liste => this.utilisateurs.set(liste),
      error: err => this.signalerErreur(err, 'Impossible de charger les utilisateurs.'),
    });
  }

  chargerComptes(): void {
    this.adminService.getComptesClients().subscribe({
      next: liste => this.comptes.set(liste),
      error: err => this.signalerErreur(err, 'Impossible de charger les comptes clients.'),
    });
  }

  chargerAssures(): void {
    this.adminService.getAssures().subscribe({
      next: liste => this.assures.set(liste),
      error: err => this.signalerErreur(err, 'Impossible de charger les assurés.'),
    });
  }

  rechargerTout(): void {
    this.chargerStatistiques();
    this.chargerUtilisateurs();
    this.chargerComptes();
    this.chargerAssures();
  }

  // ------------------------------------------------------------------ navigation

  changerVue(vue: VueAdmin): void {
    this.vue.set(vue);
    this.messageSucces = '';
    this.messageErreur = '';
    this.fermerSidebar();
  }

  allerEspaceAgent(): void {
    this.router.navigate(['/agent-dashboard']);
  }

  basculerSidebar(): void {
    this.sidebarOuverte.update(ouverte => !ouverte);
  }

  fermerSidebar(): void {
    this.sidebarOuverte.set(false);
  }

  deconnexion(): void {
    this.authService.logout();
    this.router.navigate(['/connexion']);
  }

  // ------------------------------------------------------------------ modale : ouverture

  ouvrirCreation(section: SectionModale): void {
    this.messageSucces = '';
    this.messageErreur = '';
    this.motDePasseTemporaire.set(null);
    this.emailEnvoye.set(true);
    this.modeModale.set('CREATION');
    this.sectionModale.set(section);

    if (section === 'utilisateur') {
      this.formUtilisateur = {
        idUtil: null, email: '', nomUtil: '', motDePasse: '',
        libRole: this.rolesStaff()[0]?.libRole ?? 'AGENT', statutUtilisateur: 'ACTIF',
      };
    } else if (section === 'compte') {
      this.formCompte = {
        idCompteClient: null,
        idAssure: this.assuresSansCompte()[0]?.idAssure ?? null,
        email: this.assuresSansCompte()[0]?.mail ?? '',
        statutCompte: 'ACTIF',
      };
    } else {
      this.formAssure = {
        idAssure: null, numerosAssure: '', nomAssure: '', prenomAssure: '',
        personneMorale: false, dateNaissance: '', sexe: '', civilite: '',
        nationalite: 'Malgache', paysResidence: 'Madagascar', adresse: '', contact: '', mail: '',
      };
    }
  }

  ouvrirEditionUtilisateur(u: Utilisateur): void {
    this.messageSucces = '';
    this.messageErreur = '';
    this.motDePasseTemporaire.set(null);
    this.emailEnvoye.set(true);
    this.modeModale.set('EDITION');
    this.sectionModale.set('utilisateur');
    this.formUtilisateur = {
      idUtil: u.idUtil,
      email: u.email,
      nomUtil: u.nomUtil,
      motDePasse: '',
      libRole: u.role.libRole,
      statutUtilisateur: u.statutUtilisateur,
    };
  }

  ouvrirEditionCompte(c: CompteClientVue): void {
    this.messageSucces = '';
    this.messageErreur = '';
    this.motDePasseTemporaire.set(null);
    this.emailEnvoye.set(true);
    this.modeModale.set('EDITION');
    this.sectionModale.set('compte');
    this.formCompte = {
      idCompteClient: c.idCompteClient,
      idAssure: c.idAssure ?? null,
      email: c.email,
      statutCompte: c.statutCompte,
    };
  }

  ouvrirEditionAssure(a: Assure): void {
    this.messageSucces = '';
    this.messageErreur = '';
    this.motDePasseTemporaire.set(null);
    this.emailEnvoye.set(true);
    this.modeModale.set('EDITION');
    this.sectionModale.set('assure');
    this.formAssure = {
      idAssure: a.idAssure,
      numerosAssure: a.numerosAssure,
      nomAssure: a.nomAssure,
      prenomAssure: a.prenomAssure ?? '',
      personneMorale: !!a.personneMorale,
      dateNaissance: a.dateNaissance ?? '',
      sexe: a.sexe ?? '',
      civilite: a.civilite ?? '',
      nationalite: a.nationalite ?? '',
      paysResidence: a.paysResidence ?? '',
      adresse: a.adresse ?? '',
      contact: a.contact ?? '',
      mail: a.mail ?? '',
    };
  }

  fermerModale(): void {
    this.sectionModale.set(null);
    this.motDePasseTemporaire.set(null);
    this.emailEnvoye.set(true);
  }

  /** Assurés qui n'ont pas encore de compte client (pour la création d'un compte). */
  assuresSansCompte = computed(() => {
    const pris = new Set(this.comptes().map(c => c.idAssure));
    return this.assures().filter(a => !pris.has(a.idAssure));
  });

  /** Au changement d'assuré, on pré-remplit l'e-mail de connexion avec son adresse. */
  choisirAssurePourCompte(): void {
    const assure = this.assures().find(a => a.idAssure === this.formCompte.idAssure);
    if (assure) {
      this.formCompte.email = assure.mail ?? '';
    }
  }

  // ------------------------------------------------------------------ modale : validation

  get utilisateurPret(): boolean {
    const motDePasseOk = this.modeModale() === 'EDITION'
      || this.formUtilisateur.motDePasse.trim().length >= 8;
    return !!this.formUtilisateur.email.trim()
      && !!this.formUtilisateur.nomUtil.trim()
      && !!this.formUtilisateur.libRole
      && motDePasseOk;
  }

  get comptePret(): boolean {
    if (this.modeModale() === 'CREATION') {
      return this.formCompte.idAssure !== null && !!this.formCompte.email.trim();
    }
    return !!this.formCompte.email.trim();
  }

  get assurePret(): boolean {
    const identiteOk = this.formAssure.personneMorale || !!this.formAssure.prenomAssure.trim();
    return !!this.formAssure.numerosAssure.trim() && !!this.formAssure.nomAssure.trim() && identiteOk;
  }

  get pret(): boolean {
    switch (this.sectionModale()) {
      case 'utilisateur': return this.utilisateurPret;
      case 'compte': return this.comptePret;
      case 'assure': return this.assurePret;
      default: return false;
    }
  }

  /** Section de gestion correspondant à l'onglet courant (pour le bouton « Nouveau »). */
  get sectionVue(): SectionModale {
    switch (this.vue()) {
      case 'utilisateurs': return 'utilisateur';
      case 'comptes': return 'compte';
      default: return 'assure';
    }
  }

  enregistrer(): void {
    if (!this.pret) return;
    switch (this.sectionModale()) {
      case 'utilisateur': this.enregistrerUtilisateur(); break;
      case 'compte': this.enregistrerCompte(); break;
      case 'assure': this.enregistrerAssure(); break;
    }
  }

  private enregistrerUtilisateur(): void {
    const f = this.formUtilisateur;
    const corps: Partial<Utilisateur> = {
      email: f.email.trim(),
      nomUtil: f.nomUtil.trim(),
      role: { libRole: f.libRole } as Role,
      statutUtilisateur: f.statutUtilisateur,
    };
    if (f.motDePasse.trim()) {
      corps.motDePasse = f.motDePasse;
    }

    const requete = f.idUtil === null
      ? this.utilisateurService.creer(corps)
      : this.utilisateurService.modifier(f.idUtil, corps);

    requete.subscribe({
      next: () => {
        this.messageSucces = f.idUtil === null
          ? `Utilisateur « ${corps.nomUtil} » créé.`
          : `Utilisateur « ${corps.nomUtil} » modifié.`;
        this.rechargerTout();
        this.fermerModale();
      },
      error: err => this.signalerErreur(err, "Impossible d'enregistrer cet utilisateur."),
    });
  }

  private enregistrerCompte(): void {
    const f = this.formCompte;
    if (f.idCompteClient === null) {
      this.adminService.creerCompteClient(f.idAssure!, f.email.trim()).subscribe({
        next: res => {
          this.motDePasseTemporaire.set(res.motDePasseTemporaire);
          this.emailEnvoye.set(res.emailEnvoye);
          this.messageSucces = `Compte client créé pour ${res.compte.nomAssure ?? ''} ${res.compte.prenomAssure ?? ''}.`.trim();
          this.rechargerTout();
        },
        error: err => this.signalerErreur(err, 'Impossible de créer ce compte client.'),
      });
      return;
    }
    this.adminService.modifierCompteClient(f.idCompteClient, {
      email: f.email.trim(),
      statutCompte: f.statutCompte,
    }).subscribe({
      next: () => {
        this.messageSucces = 'Compte client modifié.';
        this.rechargerTout();
        this.fermerModale();
      },
      error: err => this.signalerErreur(err, 'Impossible de modifier ce compte client.'),
    });
  }

  private enregistrerAssure(): void {
    const f = this.formAssure;
    const corps: Partial<Assure> = {
      numerosAssure: f.numerosAssure.trim(),
      nomAssure: f.nomAssure.trim(),
      prenomAssure: f.prenomAssure.trim(),
      personneMorale: f.personneMorale,
      dateNaissance: f.dateNaissance || undefined,
      sexe: f.sexe,
      civilite: f.civilite,
      nationalite: f.nationalite,
      paysResidence: f.paysResidence,
      adresse: f.adresse,
      contact: f.contact,
      mail: f.mail.trim(),
    };

    const requete = f.idAssure === null
      ? this.adminService.creerAssure(corps)
      : this.adminService.modifierAssure(f.idAssure, corps);

    requete.subscribe({
      next: () => {
        this.messageSucces = f.idAssure === null
          ? `Assuré n° ${corps.numerosAssure} créé.`
          : `Assuré n° ${corps.numerosAssure} modifié.`;
        this.rechargerTout();
        this.fermerModale();
      },
      error: err => this.signalerErreur(err, "Impossible d'enregistrer cet assuré."),
    });
  }

  // ------------------------------------------------------------------ suppression

  demanderSuppression(section: SectionModale, id: number, libelle: string): void {
    this.messageSucces = '';
    this.messageErreur = '';
    this.suppression.set({ section, id, libelle });
  }

  annulerSuppression(): void {
    this.suppression.set(null);
  }

  confirmerSuppression(): void {
    const cible = this.suppression();
    if (!cible) return;
    const requete = cible.section === 'utilisateur'
      ? this.utilisateurService.supprimer(cible.id)
      : cible.section === 'compte'
        ? this.adminService.supprimerCompteClient(cible.id)
        : this.adminService.supprimerAssure(cible.id);

    requete.subscribe({
      next: () => {
        this.messageSucces = `Suppression effectuée : ${cible.libelle}.`;
        this.suppression.set(null);
        this.rechargerTout();
      },
      error: err => {
        this.suppression.set(null);
        this.signalerErreur(err, 'Suppression impossible : cette donnée est référencée ailleurs.');
      },
    });
  }

  // ------------------------------------------------------------------ filtres

  /** Réinitialise recherche, filtres et tri de la section affichée. */
  reinitialiserFiltres(): void {
    switch (this.vue()) {
      case 'utilisateurs':
        this.rechercheUtilisateur.set('');
        this.filtreRoleUtilisateur.set('tous');
        this.filtreStatutUtilisateur.set('tous');
        this.triUtilisateur.set('nomAsc');
        break;
      case 'comptes':
        this.rechercheCompte.set('');
        this.filtreStatutCompte.set('tous');
        this.filtreConnexion.set('tous');
        this.triCompte.set('nomAsc');
        break;
      default:
        this.rechercheAssure.set('');
        this.filtreTypeAssure.set('tous');
        this.filtreMailAssure.set('tous');
        this.triAssure.set('nomAsc');
    }
  }

  // ------------------------------------------------------------------ utilitaires

  private normaliser(valeur: unknown): string {
    return (valeur ?? '').toString().toLowerCase().trim();
  }

  private correspond(texte: string, requete: string): boolean {
    const q = this.normaliser(requete);
    return q === '' || texte.includes(q);
  }

  private dateIso(valeur: unknown): string {
    return (valeur ?? '').toString().slice(0, 10);
  }

  private trierUtilisateurs(liste: Utilisateur[]): Utilisateur[] {
    return [...liste].sort((a, b) => {
      switch (this.triUtilisateur()) {
        case 'nomDesc':
          return (b.nomUtil ?? '').localeCompare(a.nomUtil ?? '');
        case 'recent':
          return this.dateIso(b.dateCreation).localeCompare(this.dateIso(a.dateCreation));
        case 'ancien':
          return this.dateIso(a.dateCreation).localeCompare(this.dateIso(b.dateCreation));
        default:
          return (a.nomUtil ?? '').localeCompare(b.nomUtil ?? '');
      }
    });
  }

  private trierComptes(liste: CompteClientVue[]): CompteClientVue[] {
    return [...liste].sort((a, b) => {
      switch (this.triCompte()) {
        case 'emailAsc':
          return (a.email ?? '').localeCompare(b.email ?? '');
        case 'recent':
          return this.dateIso(b.dateCreation).localeCompare(this.dateIso(a.dateCreation));
        case 'ancien':
          return this.dateIso(a.dateCreation).localeCompare(this.dateIso(b.dateCreation));
        default:
          return this.nomComplet(a).localeCompare(this.nomComplet(b));
      }
    });
  }

  private trierAssures(liste: Assure[]): Assure[] {
    return [...liste].sort((a, b) => {
      switch (this.triAssure()) {
        case 'nomDesc':
          return (b.nomAssure ?? '').localeCompare(a.nomAssure ?? '');
        case 'numeroAsc':
          return (a.numerosAssure ?? '').localeCompare(b.numerosAssure ?? '');
        case 'recent':
          return this.dateIso(b.dateCreation).localeCompare(this.dateIso(a.dateCreation));
        default:
          return (a.nomAssure ?? '').localeCompare(b.nomAssure ?? '');
      }
    });
  }

  private nomComplet(a: { nomAssure?: string | null; prenomAssure?: string | null }): string {
    return this.normaliser(`${a.nomAssure ?? ''} ${a.prenomAssure ?? ''}`);
  }

  private signalerErreur(err: any, defaut: string): void {
    console.error(err);
    if (err?.status === 401 || err?.status === 403) {
      this.deconnexion();
      return;
    }
    const message = err?.error?.message;
    this.messageErreur = typeof message === 'string' && message.trim() ? message : defaut;
  }

  nomUtilisateur(u: Utilisateur): string {
    return u.nomUtil || u.email;
  }

  /** Assuré rattaché au compte en cours d'édition (lecture seule). */
  get libelleCompteEdite(): string {
    const compte = this.comptes().find(c => c.idCompteClient === this.formCompte.idCompteClient);
    return compte ? this.libelleAssure(compte) : '';
  }

  libelleAssure(a: { numerosAssure?: string | null; nomAssure?: string | null; prenomAssure?: string | null }): string {
    const nom = `${a.nomAssure ?? ''} ${a.prenomAssure ?? ''}`.trim();
    return nom || a.numerosAssure || 'Assuré';
  }
}
