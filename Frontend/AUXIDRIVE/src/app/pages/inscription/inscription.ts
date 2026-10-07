import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
export class Inscription implements OnInit {
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

  messageSucces = '';
  messageErreur = '';

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['immatriculation']) {
        this.immatriculationInitiale = params['immatriculation'];
      }
    });
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

  onSubmit(): void {
    this.messageSucces = '';
    this.messageErreur = '';

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
        this.messageSucces = response.message || "Votre demande a été enregistrée avec succès.";
        setTimeout(() => this.router.navigate(['/connexion']), 3000);
      },
      error: (err) => {
        console.log("Détail complet de l'erreur reçue du backend :", err);
        
        // Extraction intelligente du message renvoyé par Spring Boot
        if (err.error && typeof err.error === 'object') {
          this.messageErreur = err.error.message || JSON.stringify(err.error);
        } else {
          this.messageErreur = err.error || "Une validation d'unicité (409) a échoué côté serveur.";
        }
      }
    });
  }
}
