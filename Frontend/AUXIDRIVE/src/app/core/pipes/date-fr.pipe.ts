import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'dateFr',
  standalone: true,
})
export class DateFrPipe implements PipeTransform {
  transform(valeur: string | null | undefined): string {
    if (!valeur) {
      return '—';
    }
    const [annee, mois, jour] = valeur.split('-');
    if (!annee || !mois || !jour) {
      return valeur;
    }
    return `${jour}/${mois}/${annee}`;
  }
}
