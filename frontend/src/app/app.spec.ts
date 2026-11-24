import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideApi } from './api/generated';
import { App } from './app';

describe('App', () => {
  it('affiche le titre et la liste des spécialités', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi('')],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('h1')?.textContent).toBe('MedHead');
    expect(element.querySelector('app-specialty-list')).not.toBeNull();
    TestBed.inject(HttpTestingController).expectOne('/api/v1/specialties').flush([]);
  });
});
