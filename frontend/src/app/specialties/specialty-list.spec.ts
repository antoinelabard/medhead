import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideApi, Specialty } from '../api/generated';
import { groupSpecialties, SpecialtyList } from './specialty-list';

const SPECIALTIES: Specialty[] = [
  { code: 'anaesthetics', group: 'Anesthésie', label: 'Anesthésie' },
  { code: 'intensive-care-medicine', group: 'Anesthésie', label: 'Soins intensifs' },
  { code: 'emergency-medicine', group: "Médecine d'urgence", label: "Médecine d'urgence" },
];

describe('SpecialtyList', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi('')],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function render() {
    const fixture = TestBed.createComponent(SpecialtyList);
    fixture.detectChanges();
    return fixture;
  }

  it('affiche les spécialités regroupées par groupe', async () => {
    const fixture = render();

    http.expectOne({ method: 'GET', url: '/api/v1/specialties' }).flush(SPECIALTIES);
    await fixture.whenStable();

    const element: HTMLElement = fixture.nativeElement;
    const sections = Array.from(element.querySelectorAll('section'));
    expect(sections.map((s) => s.querySelector('h3')?.textContent?.trim())).toEqual([
      'Anesthésie',
      "Médecine d'urgence",
    ]);
    expect(Array.from(sections[0].querySelectorAll('li')).map((li) => li.textContent?.trim())).toEqual([
      'Anesthésie',
      'Soins intensifs',
    ]);
    expect(element.querySelector('[role="alert"]')).toBeNull();
  });

  it("affiche un message d'erreur quand l'API est indisponible", async () => {
    const fixture = render();

    http
      .expectOne('/api/v1/specialties')
      .flush('Bad Gateway', { status: 502, statusText: 'Bad Gateway' });
    await fixture.whenStable();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('[role="alert"]')?.textContent?.trim()).toBe(
      'Impossible de charger les spécialités',
    );
    expect(element.querySelectorAll('li').length).toBe(0);
  });

  it('affiche un message de chargement avant la réponse', () => {
    const fixture = render();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Chargement des spécialités');
    http.expectOne('/api/v1/specialties').flush([]);
  });
});

describe('groupSpecialties', () => {
  it("conserve l'ordre du référentiel pour les groupes et les spécialités", () => {
    const groups = groupSpecialties([SPECIALTIES[2], SPECIALTIES[0], SPECIALTIES[1]]);

    expect(groups.map((g) => g.name)).toEqual(["Médecine d'urgence", 'Anesthésie']);
    expect(groups[1].specialties.map((s) => s.code)).toEqual(['anaesthetics', 'intensive-care-medicine']);
  });
});
