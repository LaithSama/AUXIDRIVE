import { Component, OnDestroy, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { InscriptionService } from '../../services/inscription-service';
import { Assure } from '../../core/interfaces/assure';
import { AssureIdentifiantsLegaux } from '../../core/interfaces/assure-identifiants-legaux';
import { AssureProfession } from '../../core/interfaces/assure-profession';

@Component({
  selector: 'app-inscription',
  imports: [FormsModule, RouterLink],
  templateUrl: './inscription.html',
  styleUrl: './inscription.scss',
})
export class Inscription implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private inscriptionService = inject(InscriptionService);

  immatriculationInitiale = '';

  assure: Assure = {
    nomAssure: '',
    prenomAssure: '',
    personneMorale: false,
    dateNaissance: '',
    sexe: 'M',
    civilite: 'M.',
    nationalite: 'Malagasy',
    paysResidence: 'Madagascar',
    adresse: '',
    contact: '',
    mail: '',
    idAssure: null as any,       
    numerosAssure: null as any,  
    dateCreation: null as any    
  };

  identifiantsLegaux: AssureIdentifiantsLegaux = {
    cin: '',
    nif: '',
    cif: ''
  };

  profession: AssureProfession = {
    professionAssure: 'Sans profession / Autre',
    numMle: '',
    numMleAncien: '',
    steFacturation: '',
    steAdresse: '',
    steTel: '',
    steMail: '',
    nomResponsable: ''
  };

  fichierCin: File | null = null;
  fichierCarteGrise: File | null = null;
  fichierCarteRose: File | null = null;
  fichierModeleN1: File | null = null;

  messageErreur = '';

  enCours = false;
  dossierEnvoye = false;
  numeroDossier: number | null = null;
  compteArebours = 10;
  private minuteur: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['immatriculation']) {
        this.immatriculationInitiale = params['immatriculation'];
      }
    });
  }

  ngOnDestroy(): void {
    this.arreterMinuteur();
  }

  onFileChange(event: any, type: string): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      const selectedFile = files[0];
      if (type === 'cin') this.fichierCin = selectedFile;
      if (type === 'carteGrise') this.fichierCarteGrise = selectedFile;
      if (type === 'carteRose') this.fichierCarteRose = selectedFile;
      if (type === 'modeleN1') this.fichierModeleN1 = selectedFile;
    }
  }

  onSubmit(formulaire?: NgForm): void {
    if (this.enCours || this.dossierEnvoye) {
      return;
    }
    this.messageErreur = '';
    if (formulaire?.invalid) {
      formulaire.control.markAllAsTouched();
      this.messageErreur = 'Complétez les champs obligatoires et vérifiez votre adresse e-mail.';
      return;
    }
    this.enCours = true;

    const listeFichiers = {
      cin: this.fichierCin,
      carteGrise: this.fichierCarteGrise,
      carteRose: this.fichierCarteRose,
      modeleN1: this.fichierModeleN1
    };

    // 1. Nettoyage strict de l'assuré (mise à null des champs vides ou systèmes)
    const assureNettoye: Assure = {
      ...this.assure,
      nomAssure: this.assure.nomAssure?.trim() || '',
      prenomAssure: this.assure.prenomAssure?.trim() || '',
      mail: this.assure.mail?.trim() || '',
      contact: this.assure.contact?.trim() || '',
      adresse: this.assure.adresse?.trim() || '',
      idAssure: null as any,
      numerosAssure: null as any,
      dateCreation: null as any
    };

    // 2. Remplacement des chaînes vides par "null" pour éviter le conflit 409 en base de données
    const identifiantsNettoyes = {
      cin: this.identifiantsLegaux.cin?.trim() || null,
      nif: this.identifiantsLegaux.nif?.trim() || null,
      cif: this.identifiantsLegaux.cif?.trim() || null
    };

    // 3. Remplacement des chaînes vides par "null" pour la partie professionnelle
    const professionNettoye = {
      professionAssure: this.profession.professionAssure?.trim() || 'Particulier',
      numMle: this.profession.numMle?.trim() || null,
      numMleAncien: this.profession.numMleAncien?.trim() || null,
      steFacturation: this.profession.steFacturation?.trim() || null,
      steAdresse: this.profession.steAdresse?.trim() || null,
      steTel: this.profession.steTel?.trim() || null,
      steMail: this.profession.steMail?.trim() || null,
      nomResponsable: this.profession.nomResponsable?.trim() || null
    };

    // 4. Envoi des structures nettoyées et conformes au service
    this.inscriptionService.soumettre(assureNettoye, professionNettoye as any, identifiantsNettoyes as any, listeFichiers).subscribe({
      next: (response) => {
        this.enCours = false;
        this.numeroDossier = response.idInscription ?? null;
        this.dossierEnvoye = true;
        this.lancerCompteArebours();
      },
      error: (err) => {
        this.enCours = false;
        const corps = err.error;
        const detail = typeof corps === 'string' ? corps
          : typeof corps?.detail === 'string' ? corps.detail
          : typeof corps?.message === 'string' ? corps.message : '';
        if (err.status === 0) {
          this.messageErreur = 'Connexion au serveur impossible. Vérifiez votre connexion et réessayez.';
        } else if (err.status === 409) {
          this.messageErreur = detail.trim() || 'Cette adresse e-mail ou cet identifiant est déjà utilisé.';
        } else if (err.status === 400) {
          this.messageErreur = detail.trim() || 'Vérifiez les informations et les pièces justificatives de votre dossier.';
        } else if (err.status === 413) {
          this.messageErreur = 'Fichier trop volumineux. Chaque pièce doit faire 5 Mo maximum.';
        } else {
          this.messageErreur = 'Impossible de transmettre le dossier pour le moment. Veuillez réessayer.';
        }
      }
    });
  }

  allerConnexion(): void {
    this.arreterMinuteur();
    this.router.navigate(['/connexion']);
  }

  private lancerCompteArebours(): void {
    this.compteArebours = 10;
    this.minuteur = setInterval(() => {
      this.compteArebours -= 1;
      if (this.compteArebours <= 0) {
        this.allerConnexion();
      }
    }, 1000);
  }

  private arreterMinuteur(): void {
    if (this.minuteur !== null) {
      clearInterval(this.minuteur);
      this.minuteur = null;
    }
  }
}
