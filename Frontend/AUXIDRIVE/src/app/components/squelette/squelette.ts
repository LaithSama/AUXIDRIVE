import { Component, computed, input } from '@angular/core';

/** Chargement accessible, adapté aux tableaux et aux cartes. */
@Component({
  selector: 'app-squelette',
  host: {
    class: 'squelette',
    role: 'status',
    '[attr.aria-label]': 'libelle()',
    '[class.squelette-cartes]': "mode() === 'cartes' && nombre() === 3",
    '[class.squelette-deux-colonnes]': "mode() === 'cartes' && nombre() === 2",
    '[class.squelette-liste]': "mode() === 'liste'",
  },
  template: `
    @for (i of indices(); track i) {
      @if (mode() === 'cartes') {
        <div class="squelette-carte" aria-hidden="true">
          <span class="squelette-ligne"></span><span class="squelette-ligne squelette-ligne-large"></span><span class="squelette-ligne"></span>
        </div>
      } @else {
        <span class="squelette-ligne" aria-hidden="true"></span>
      }
    }
    <span class="visually-hidden">{{ libelle() }}…</span>
  `,
})
export class Squelette {
  libelle = input.required<string>();
  mode = input<'cartes' | 'liste'>('cartes');
  nombre = input(3);
  indices = computed(() => Array.from({ length: this.nombre() }, (_, i) => i));
}
