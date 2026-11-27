import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MockDataService } from '../../core/services/mock-data.service';
import { UserRole } from '../../core/models';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentRole: UserRole = 'student';
  pageTitle = 'Tableau de Bord';
  currentDate = new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

  constructor(public mockService: MockDataService, private router: Router) {}

  ngOnInit(): void {
    this.mockService.currentRole$.subscribe(r => this.currentRole = r);

    this.updateTitle(this.router.url);
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updateTitle(event.urlAfterRedirects || event.url);
    });
  }

  private updateTitle(url: string): void {
    if (url.includes('academic')) {
      this.pageTitle = 'Gestion Académique & Pédagogique';
    } else if (url.includes('smart-space')) {
      this.pageTitle = 'Espaces, Salles & Examens en Ligne';
    } else if (url.includes('administration')) {
      this.pageTitle = 'Administration Universitaire & Certifications';
    } else if (url.includes('portal')) {
      this.pageTitle = 'Mon Espace Personnel';
    } else {
      this.pageTitle = 'Tableau de Bord';
    }
  }
}
