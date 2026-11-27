import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AcademicComponent } from './components/academic/academic.component';
import { SmartSpaceComponent } from './components/smart-space/smart-space.component';
import { AdministrationComponent } from './components/administration/administration.component';
import { PortalComponent } from './components/portal/portal.component';
import { ArchitectureComponent } from './components/architecture/architecture.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'academic', component: AcademicComponent },
  { path: 'smart-space', component: SmartSpaceComponent },
  { path: 'administration', component: AdministrationComponent },
  { path: 'portal', component: PortalComponent },
  { path: 'architecture', component: ArchitectureComponent },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'top' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
