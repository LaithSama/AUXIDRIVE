import { Component, input } from '@angular/core';

/** Une entrée de légende : pastille, libellé, valeur et part indicative. */
export interface ItemLegende {
  libelle: string;
  couleur: string;
  valeur: string;
  pourcentage?: number;
}

/**
 * Légende partagée par les graphiques : la couleur de chaque série, son
 * libellé et son effectif. Les valeurs sont toujours affichées, jamais
 * uniquement au survol, pour que le graphique reste lisible sur une capture.
 */
@Component({
  selector: 'app-graphique-legende',
  templateUrl: './graphique-legende.html',
  styleUrl: './graphique-legende.scss',
})
export class GraphiqueLegende {
  readonly items = input<ItemLegende[]>([]);
}
