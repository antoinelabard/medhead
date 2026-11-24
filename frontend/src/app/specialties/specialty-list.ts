import { Component, inject, signal } from '@angular/core';
import { Specialty, SpecialtiesService } from '../api/generated';

export interface SpecialtyGroup {
  readonly name: string;
  readonly specialties: readonly Specialty[];
}

type State =
  | { readonly status: 'loading' }
  | { readonly status: 'loaded'; readonly groups: readonly SpecialtyGroup[] }
  | { readonly status: 'error' };

/** Regroupe les spécialités par groupe, dans l'ordre du référentiel. */
export function groupSpecialties(specialties: readonly Specialty[]): SpecialtyGroup[] {
  const groups = new Map<string, Specialty[]>();
  for (const specialty of specialties) {
    const group = groups.get(specialty.group) ?? [];
    group.push(specialty);
    groups.set(specialty.group, group);
  }
  return [...groups].map(([name, items]) => ({ name, specialties: items }));
}

@Component({
  selector: 'app-specialty-list',
  template: `
    <h2>Spécialités NHS</h2>
    @switch (state().status) {
      @case ('loading') {
        <p>Chargement des spécialités…</p>
      }
      @case ('error') {
        <p role="alert">Impossible de charger les spécialités</p>
      }
      @case ('loaded') {
        @for (group of groups(); track group.name) {
          <section>
            <h3>{{ group.name }}</h3>
            <ul>
              @for (specialty of group.specialties; track specialty.code) {
                <li>{{ specialty.label }}</li>
              }
            </ul>
          </section>
        }
      }
    }
  `,
})
export class SpecialtyList {
  protected readonly state = signal<State>({ status: 'loading' });

  constructor() {
    inject(SpecialtiesService)
      .listSpecialties()
      .subscribe({
        next: (specialties) => this.state.set({ status: 'loaded', groups: groupSpecialties(specialties) }),
        error: () => this.state.set({ status: 'error' }),
      });
  }

  protected groups(): readonly SpecialtyGroup[] {
    const state = this.state();
    return state.status === 'loaded' ? state.groups : [];
  }
}
