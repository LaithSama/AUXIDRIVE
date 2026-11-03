import { Component, input, output } from '@angular/core';

/** Présentation commune des listes vides, avec action facultative. */
@Component({
  selector: 'app-etat-vide',
  template: `
    <div class="etat-vide">
      <span class="etat-vide-icone" aria-hidden="true"><i [class]="'bi ' + icone()"></i></span>
      <strong class="etat-vide-titre">{{ titre() }}</strong>
      <span class="etat-vide-description">{{ description() }}</span>
      @if (action()) {
        @if (lien()) {
          <a class="btn-contour-sombre etat-vide-action" [href]="lien()">{{ action() }}</a>
        } @else {
          <button type="button" class="btn-contour-sombre etat-vide-action" (click)="actionCliquee.emit()">{{ action() }}</button>
        }
      }
    </div>
  `,
})
export class EtatVide {
  titre = input.required<string>();
  description = input.required<string>();
  icone = input('bi-inbox');
  action = input('');
  lien = input('');
  actionCliquee = output<void>();
}
