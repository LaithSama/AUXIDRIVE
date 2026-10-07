import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'montant',
  standalone: true,
})
export class MontantPipe implements PipeTransform {
  transform(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${Math.round(valeur).toLocaleString('fr-FR')} Ar`;
  }
}
