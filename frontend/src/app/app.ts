import { Component } from '@angular/core';
import { SpecialtyList } from './specialties/specialty-list';

@Component({
  selector: 'app-root',
  imports: [SpecialtyList],
  template: `
    <main>
      <h1>MedHead</h1>
      <app-specialty-list />
    </main>
  `,
  styleUrl: './app.css',
})
export class App {}
